package com.wywhdgg.dzb.service.impl;

import com.wywhdgg.dzb.dao.ConfEnvDao;
import com.wywhdgg.dzb.dao.ConfNodeDao;
import com.wywhdgg.dzb.dao.ConfNodeLogDao;
import com.wywhdgg.dzb.dao.ConfNodeMsgDao;
import com.wywhdgg.dzb.dao.ConfProjectDao;
import com.wywhdgg.dzb.entity.ConfEnv;
import com.wywhdgg.dzb.entity.ConfNode;
import com.wywhdgg.dzb.entity.ConfNodeLog;
import com.wywhdgg.dzb.entity.ConfNodeMsg;
import com.wywhdgg.dzb.entity.ConfProject;
import com.wywhdgg.dzb.entity.ConfUser;
import com.wywhdgg.dzb.serivce.ConfNodeService;
import com.wywhdgg.dzb.util.PropUtil;
import com.wywhdgg.dzb.util.RegexUtil;
import com.wywhdgg.dzb.util.Result;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

/***
 *@author dzb
 *@date 2019/7/21 23:03
 *@Description:
 *@version 1.0
 */
@Slf4j
@Service
public class ConfNodeServiceImpl implements  ConfNodeService, InitializingBean, DisposableBean {


    @Resource
    private ConfNodeDao confNodeDao;
    @Resource
    private ConfProjectDao confProjectDao;
    /*@Resource
    private ConfZKManager confZKManager;*/
    @Resource
    private ConfNodeLogDao confNodeLogDao;
    @Resource
    private ConfEnvDao confEnvDao;
    @Resource
    private ConfNodeMsgDao confNodeMsgDao;


    @Value("${conf.confdata.filepath}")
    private String confDataFilePath;
    @Value("${conf.access.token}")
    private String accessToken;

    private int confBeatTime = 30;

    @Override
    public int pageListCount(int offset, int pagesize, String env, String appname, String key) {
        return confNodeDao.pageListCount(offset,pagesize,env,appname,key);
    }

    @Override
    public boolean ifHasProjectPermission(ConfUser loginUser, String loginEnv, String appname){
        if (loginUser.getPermission() == 1) {
            return true;
        }
        if (ArrayUtils.contains(StringUtils.split(loginUser.getPermissionData(), ","), (appname.concat("#").concat(loginEnv)) )) {
            return true;
        }
        return false;
    }

    @Override
    public Map<String,Object> pageList(int offset,
        int pagesize,
        String appname,
        String key,
        ConfUser loginUser,
        String loginEnv) {

        // project permission
        if (StringUtils.isBlank(loginEnv) || StringUtils.isBlank(appname) || !ifHasProjectPermission(loginUser, loginEnv, appname)) {
            //return new Result<String>(500, "您没有该项目的配置权限,请联系管理员开通");
            Map<String, Object> emptyMap = new HashMap<String, Object>();
            emptyMap.put("data", new ArrayList<>());
            emptyMap.put("recordsTotal", 0);
            emptyMap.put("recordsFiltered", 0);
            return emptyMap;
        }

        // confNode in mysql
        List<ConfNode> data = confNodeDao.pageList(offset, pagesize, loginEnv, appname, key);
        int list_count = confNodeDao.pageListCount(offset, pagesize, loginEnv, appname, key);

        // fill value in zk
		/*if (CollectionUtils.isNotEmpty(data)) {
			for (ConfNode node: data) {
				String realNodeValue = confZKManager.get(node.getEnv(), node.getKey());
				node.setZkValue(realNodeValue);
			}
		}*/

        // package result
        Map<String, Object> maps = new HashMap<String, Object>();
        maps.put("data", data);
        maps.put("recordsTotal", list_count);		// 总记录数
        maps.put("recordsFiltered", list_count);	// 过滤后的总记录数
        return maps;

    }

    @Override
    public Result<String> delete(String key, ConfUser loginUser, String loginEnv) {
            if (StringUtils.isBlank(key)) {
            return new Result<String>(500, "参数缺失");
        }
        ConfNode existNode = confNodeDao.load(loginEnv, key);
        if (existNode == null) {
            return new Result<String>(500, "参数非法");
        }

        // project permission
        if (!ifHasProjectPermission(loginUser, loginEnv, existNode.getAppname())) {
            return new Result<String>(500, "您没有该项目的配置权限,请联系管理员开通");
        }

        //confZKManager.delete(loginEnv, key);
        confNodeDao.delete(loginEnv, key);
        confNodeLogDao.deleteTimeout(loginEnv, key, 0);

        // conf msg
        sendConfMsg(loginEnv, key, null);

        return Result.SUCCESS;
    }

    // conf broadcast msg
    private void sendConfMsg(String env, String key, String value){

        ConfNodeMsg confNodeMsg = new ConfNodeMsg();
        confNodeMsg.setEnv(env);
        confNodeMsg.setKey(key);
        confNodeMsg.setValue(value);

        confNodeMsgDao.add(confNodeMsg);
    }

    @Override
    public Result<String> add(ConfNode confNode, ConfUser loginUser, String loginEnv) {

        // valid
        if (StringUtils.isBlank(confNode.getAppname())) {
            return new Result<String>(500, "AppName不可为空");
        }

        // project permission
        if (!ifHasProjectPermission(loginUser, loginEnv, confNode.getAppname())) {
            return new Result<String>(500, "您没有该项目的配置权限,请联系管理员开通");
        }

        // valid group
        ConfProject group = confProjectDao.load(confNode.getAppname());
        if (group==null) {
            return new Result<String>(500, "AppName非法");
        }

        // valid env
        if (StringUtils.isBlank(confNode.getEnv())) {
            return new Result<String>(500, "配置Env不可为空");
        }
        ConfEnv confEnv = confEnvDao.load(confNode.getEnv());
        if (confEnv == null) {
            return new Result<String>(500, "配置Env非法");
        }

        // valid key
        if (StringUtils.isBlank(confNode.getKey())) {
            return new Result<String>(500, "配置Key不可为空");
        }
        confNode.setKey(confNode.getKey().trim());

        ConfNode existNode = confNodeDao.load(confNode.getEnv(), confNode.getKey());
        if (existNode != null) {
            return new Result<String>(500, "配置Key已存在，不可重复添加");
        }
        if (!confNode.getKey().startsWith(confNode.getAppname())) {
            return new Result<String>(500, "配置Key格式非法");
        }

        // valid title
        if (StringUtils.isBlank(confNode.getTitle())) {
            return new Result<String>(500, "配置描述不可为空");
        }

        // value force null to ""
        if (confNode.getValue() == null) {
            confNode.setValue("");
        }

        // add node
        //confZKManager.set(confNode.getEnv(), confNode.getKey(), confNode.getValue());
        confNodeDao.insert(confNode);

        // node log
        ConfNodeLog nodeLog = new ConfNodeLog();
        nodeLog.setEnv(confNode.getEnv());
        nodeLog.setKey(confNode.getKey());
        nodeLog.setTitle(confNode.getTitle() + "(配置新增)" );
        nodeLog.setValue(confNode.getValue());
        nodeLog.setOptuser(loginUser.getUsername());
        confNodeLogDao.add(nodeLog);

        // conf msg
        sendConfMsg(confNode.getEnv(), confNode.getKey(), confNode.getValue());

        return Result.SUCCESS;
    }

    @Override
    public Result<String> update(ConfNode confNode, ConfUser loginUser, String loginEnv) {

        // valid
        if (StringUtils.isBlank(confNode.getKey())) {
            return new Result<String>(500, "配置Key不可为空");
        }
        ConfNode existNode = confNodeDao.load(confNode.getEnv(), confNode.getKey());
        if (existNode == null) {
            return new Result<String>(500, "配置Key非法");
        }

        // project permission
        if (!ifHasProjectPermission(loginUser, loginEnv, existNode.getAppname())) {
            return new Result<String>(500, "您没有该项目的配置权限,请联系管理员开通");
        }

        if (StringUtils.isBlank(confNode.getTitle())) {
            return new Result<String>(500, "配置描述不可为空");
        }

        // value force null to ""
        if (confNode.getValue() == null) {
            confNode.setValue("");
        }

        // update conf
        //confZKManager.set(confNode.getEnv(), confNode.getKey(), confNode.getValue());

        existNode.setTitle(confNode.getTitle());
        existNode.setValue(confNode.getValue());
        int ret = confNodeDao.update(existNode);
        if (ret < 1) {
            return Result.FAIL;
        }

        // node log
        ConfNodeLog nodeLog = new ConfNodeLog();
        nodeLog.setEnv(existNode.getEnv());
        nodeLog.setKey(existNode.getKey());
        nodeLog.setTitle(existNode.getTitle() + "(配置更新)" );
        nodeLog.setValue(existNode.getValue());
        nodeLog.setOptuser(loginUser.getUsername());
        confNodeLogDao.add(nodeLog);
        confNodeLogDao.deleteTimeout(existNode.getEnv(), existNode.getKey(), 10);

        // conf msg
        sendConfMsg(confNode.getEnv(), confNode.getKey(), confNode.getValue());

        return Result.SUCCESS;
    }

	/*@Override
	public Result<String> syncConf(String appname, ConfUser loginUser, String loginEnv) {

		// valid
		ConfEnv confEnv = confEnvDao.load(loginEnv);
		if (confEnv == null) {
			return new Result<String>(500, "配置Env非法");
		}
		ConfProject group = confProjectDao.load(appname);
		if (group==null) {
			return new Result<String>(500, "AppName非法");
		}

		// project permission
		if (!ifHasProjectPermission(loginUser, loginEnv, appname)) {
			return new Result<String>(500, "您没有该项目的配置权限,请联系管理员开通");
		}

		List<ConfNode> confNodeList = confNodeDao.pageList(0, 10000, loginEnv, appname, null);
		if (CollectionUtils.isEmpty(confNodeList)) {
			return new Result<String>(500, "操作失败，该项目下不存在配置项");
		}

		// un sync node
		List<ConfNode> unSyncConfNodeList = new ArrayList<>();
		for (ConfNode node: confNodeList) {
			String realNodeValue = confZKManager.get(node.getEnv(), node.getKey());
			if (!node.getValue().equals(realNodeValue)) {
				unSyncConfNodeList.add(node);
			}
		}

		if (CollectionUtils.isEmpty(unSyncConfNodeList)) {
			return new Result<String>(500, "操作失败，该项目下不存未同步的配置项");
		}

		// do sync
		String logContent = "操作成功，共计同步 " + unSyncConfNodeList.size() + " 条配置：";
		for (ConfNode node: unSyncConfNodeList) {

			confZKManager.set(node.getEnv(), node.getKey(), node.getValue());

			// node log
			ConfNodeLog nodeLog = new ConfNodeLog();
			nodeLog.setEnv(node.getEnv());
			nodeLog.setKey(node.getKey());
			nodeLog.setTitle(node.getTitle() + "(全量同步)" );
			nodeLog.setValue(node.getValue());
			nodeLog.setOptuser(loginUser.getUsername());
			confNodeLogDao.add(nodeLog);
			confNodeLogDao.deleteTimeout(node.getEnv(), node.getKey(), 10);

			logContent += "<br>" + node.getKey();
		}
		logContent.substring(logContent.length() - 1);

		return new Result<String>(Result.SUCCESS.getCode(), logContent);
	}*/


    // ---------------------- rest api ----------------------

    @Override
    public Result<Map<String, String>> find(String accessToken, String env, List<String> keys) {

        // valid
        if (this.accessToken!=null && this.accessToken.trim().length()>0 && !this.accessToken.equals(accessToken)) {
            return new Result<Map<String, String>>(Result.FAIL.getCode(), "AccessToken Invalid.");
        }
        if (env==null || env.trim().length()==0) {
            return new Result<>(Result.FAIL.getCode(), "env Invalid.");
        }
        if (keys==null || keys.size()==0) {
            return new Result<>(Result.FAIL.getCode(), "keys Invalid.");
        }
		/*for (String key: keys) {
			if (key==null || key.trim().length()<4 || key.trim().length()>100) {
				return new Result<>(Result.FAIL.getCode(), "Key Invalid[4~100]");
			}
			if (!RegexUtil.matches(RegexUtil.abc_number_line_point_pattern, key)) {
				return new Result<>(Result.FAIL.getCode(), "Key format Invalid");
			}
		}*/

        // result
        Map<String, String> result = new HashMap<String, String>();
        for (String key: keys) {

            // get val
            String value = null;
            if (key==null || key.trim().length()<4 || key.trim().length()>100
                || !RegexUtil.matches(RegexUtil.abc_number_line_point_pattern, key) ) {
                // invalid key, pass
            } else {
                value = getFileConfData(env, key);
            }

            // parse null
            if (value == null) {
                value = "";
            }

            // put
            result.put(key, value);
        }

        return new Result<Map<String, String>>(result);
    }

    @Override
    public DeferredResult<Result<String>>   monitor(String accessToken, String env, List<String> keys) {
        // init
        DeferredResult deferredResult = new DeferredResult(confBeatTime * 1000L, new Result<>(Result.SUCCESS_CODE, "Monitor timeout, no key updated."));

        // valid
        if (this.accessToken!=null && this.accessToken.trim().length()>0 && !this.accessToken.equals(accessToken)) {
            deferredResult.setResult(new Result<>(Result.FAIL.getCode(), "AccessToken Invalid."));
            return deferredResult;
        }
        if (env==null || env.trim().length()==0) {
            deferredResult.setResult(new Result<>(Result.FAIL.getCode(), "env Invalid."));
            return deferredResult;
        }
        if (keys==null || keys.size()==0) {
            deferredResult.setResult(new Result<>(Result.FAIL.getCode(), "keys Invalid."));
            return deferredResult;
        }
		/*for (String key: keys) {
			if (key==null || key.trim().length()<4 || key.trim().length()>100) {
				deferredResult.setResult(new Result<>(Result.FAIL.getCode(), "Key Invalid[4~100]"));
				return deferredResult;
			}
			if (!RegexUtil.matches(RegexUtil.abc_number_line_point_pattern, key)) {
				deferredResult.setResult(new Result<>(Result.FAIL.getCode(), "Key format Invalid"));
				return deferredResult;
			}
		}*/

        // monitor by client
        for (String key: keys) {

            // invalid key, pass
            if (key==null || key.trim().length()<4 || key.trim().length()>100
                || !RegexUtil.matches(RegexUtil.abc_number_line_point_pattern, key) ) {
                continue;
            }

            // monitor each key
            String fileName = parseConfDataFileName(env, key);

            List<DeferredResult> deferredResultList = confDeferredResultMap.get(fileName);
            if (deferredResultList == null) {
                deferredResultList = new ArrayList<>();
                confDeferredResultMap.put(fileName, deferredResultList);
            }

            deferredResultList.add(deferredResult);
        }

        return deferredResult;
    }





    // ---------------------- start stop ----------------------

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("afterPropertiesSet.......");
        startThead();
    }

    @Override
    public void destroy() throws Exception {
        stopThread();
    }


    // ---------------------- thread ----------------------

    private ExecutorService executorService = Executors.newCachedThreadPool();
    private volatile boolean executorStoped = false;

    private volatile List<Integer> readedMessageIds = Collections.synchronizedList(new ArrayList<Integer>());

    private Map<String, List<DeferredResult>> confDeferredResultMap = new ConcurrentHashMap<>();

    public void startThead() throws Exception {

        /**
         * brocast conf-data msg, sync to file, for "add、update、delete"
         */
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (!executorStoped) {
                    try {
                        // new message, filter readed
                        List<ConfNodeMsg> messageList = confNodeMsgDao.findMsg(readedMessageIds);
                        if (messageList!=null && messageList.size()>0) {
                            for (ConfNodeMsg message: messageList) {
                                readedMessageIds.add(message.getId());


                                // sync file
                                setFileConfData(message.getEnv(), message.getKey(), message.getValue());
                            }
                        }

                        // clean old message;
                        if ( (System.currentTimeMillis()/1000) % confBeatTime ==0) {
                            confNodeMsgDao.cleanMessage(confBeatTime);
                            readedMessageIds.clear();
                        }
                    } catch (Exception e) {
                        if (!executorStoped) {
                            log.error(e.getMessage(), e);
                        }
                    }
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (Exception e) {
                        if (!executorStoped) {
                            log.error(e.getMessage(), e);
                        }
                    }
                }
            }
        });


        /**
         *  sync total conf-data, db + file      (1+N/30s)
         *
         *  clean deleted conf-data file
         */
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (!executorStoped) {

                    // align to beattime
                    try {
                        long sleepSecond = confBeatTime - (System.currentTimeMillis()/1000)%confBeatTime;
                        if (sleepSecond>0 && sleepSecond<confBeatTime) {
                            TimeUnit.SECONDS.sleep(sleepSecond);
                        }
                    } catch (Exception e) {
                        if (!executorStoped) {
                            log.error(e.getMessage(), e);
                        }
                    }

                    try {

                        // sync registry-data, db + file
                        int offset = 0;
                        int pagesize = 1000;
                        List<String> confDataFileList = new ArrayList<>();

                        List<ConfNode> confNodeList = confNodeDao.pageList(offset, pagesize, null, null, null);
                        while (confNodeList!=null && confNodeList.size()>0) {

                            for (ConfNode confNoteItem: confNodeList) {

                                // sync file
                                String confDataFile = setFileConfData(confNoteItem.getEnv(), confNoteItem.getKey(), confNoteItem.getValue());

                                // collect confDataFile
                                confDataFileList.add(confDataFile);
                            }


                            offset += 1000;
                            confNodeList = confNodeDao.pageList(offset, pagesize, null, null, null);
                        }

                        // clean old registry-data file
                        cleanFileConfData(confDataFileList);

                        log.debug(">>>>>>>>>>> xxl-conf, sync totel conf data success, sync conf count = {}", confDataFileList.size());
                    } catch (Exception e) {
                        if (!executorStoped) {
                            log.error(e.getMessage(), e);
                        }
                    }
                    try {
                        TimeUnit.SECONDS.sleep(confBeatTime);
                    } catch (Exception e) {
                        if (!executorStoped) {
                            log.error(e.getMessage(), e);
                        }
                    }
                }
            }
        });



    }

    private void stopThread(){
        executorStoped = true;
        executorService.shutdownNow();
    }


    // ---------------------- file opt ----------------------

    // get
    public String getFileConfData(String env, String key){

        // fileName
        String confFileName = parseConfDataFileName(env, key);

        // read
        Properties existProp = PropUtil.loadFileProp(confFileName);
        if (existProp!=null && existProp.containsKey("value")) {
            return existProp.getProperty("value");
        }
        return null;
    }

    private String parseConfDataFileName(String env, String key){
        // fileName
        String fileName = confDataFilePath
            .concat(File.separator).concat(env)
            .concat(File.separator).concat(key)
            .concat(".properties");
        return fileName;
    }

    // set
    private String setFileConfData(String env, String key, String value){

        // fileName
        String confFileName = parseConfDataFileName(env, key);

        // valid repeat update
        Properties existProp = PropUtil.loadFileProp(confFileName);
        if (existProp != null
            && value!=null
            && value.equals(existProp.getProperty("value"))
            ) {
            return new File(confFileName).getPath();
        }

        // write
        Properties prop = new Properties();
        if (value == null) {
            prop.setProperty("value-deleted", "true");
        } else {
            prop.setProperty("value", value);
        }

        PropUtil.writeFileProp(prop, confFileName);
        log.info(">>>>>>>>>>> xxl-conf, setFileConfData: confFileName={}, value={}", confFileName, value);

        // brocast monitor client
        List<DeferredResult> deferredResultList = confDeferredResultMap.get(confFileName);
        if (deferredResultList != null) {
            confDeferredResultMap.remove(confFileName);
            for (DeferredResult deferredResult: deferredResultList) {
                deferredResult.setResult(new Result<>(Result.SUCCESS_CODE, "Monitor key update."));
            }
        }

        return new File(confFileName).getPath();
    }

    // clean
    public void cleanFileConfData(List<String> confDataFileList){
        filterChildPath(new File(confDataFilePath), confDataFileList);
    }

    public void filterChildPath(File parentPath, final List<String> confDataFileList){
        if (!parentPath.exists() || parentPath.list()==null || parentPath.list().length==0) {
            return;
        }
        File[] childFileList = parentPath.listFiles();
        for (File childFile: childFileList) {
            if (childFile.isFile() && !confDataFileList.contains(childFile.getPath())) {
                childFile.delete();

                log.info(">>>>>>>>>>> xxl-conf, cleanFileConfData, ConfDataFile={}", childFile.getPath());
            }
            if (childFile.isDirectory()) {
                if (parentPath.listFiles()!=null && parentPath.listFiles().length>0) {
                    filterChildPath(childFile, confDataFileList);
                } else {
                    childFile.delete();
                }

            }
        }

    }
    
}
