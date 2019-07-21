package com.wywhdgg.dzb.service;

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
    private ConfNodeDao xxlConfNodeDao;
    @Resource
    private ConfProjectDao xxlConfProjectDao;
    /*@Resource
    private ConfZKManager xxlConfZKManager;*/
    @Resource
    private ConfNodeLogDao xxlConfNodeLogDao;
    @Resource
    private ConfEnvDao xxlConfEnvDao;
    @Resource
    private ConfNodeMsgDao xxlConfNodeMsgDao;


    @Value("${conf.confdata.filepath}")
    private String confDataFilePath;
    @Value("${conf.access.token}")
    private String accessToken;

    private int confBeatTime = 30;


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

        // xxlConfNode in mysql
        List<ConfNode> data = xxlConfNodeDao.pageList(offset, pagesize, loginEnv, appname, key);
        int list_count = xxlConfNodeDao.pageListCount(offset, pagesize, loginEnv, appname, key);

        // fill value in zk
		/*if (CollectionUtils.isNotEmpty(data)) {
			for (ConfNode node: data) {
				String realNodeValue = xxlConfZKManager.get(node.getEnv(), node.getKey());
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
        ConfNode existNode = xxlConfNodeDao.load(loginEnv, key);
        if (existNode == null) {
            return new Result<String>(500, "参数非法");
        }

        // project permission
        if (!ifHasProjectPermission(loginUser, loginEnv, existNode.getAppname())) {
            return new Result<String>(500, "您没有该项目的配置权限,请联系管理员开通");
        }

        //xxlConfZKManager.delete(loginEnv, key);
        xxlConfNodeDao.delete(loginEnv, key);
        xxlConfNodeLogDao.deleteTimeout(loginEnv, key, 0);

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

        xxlConfNodeMsgDao.add(confNodeMsg);
    }

    @Override
    public Result<String> add(ConfNode xxlConfNode, ConfUser loginUser, String loginEnv) {

        // valid
        if (StringUtils.isBlank(xxlConfNode.getAppname())) {
            return new Result<String>(500, "AppName不可为空");
        }

        // project permission
        if (!ifHasProjectPermission(loginUser, loginEnv, xxlConfNode.getAppname())) {
            return new Result<String>(500, "您没有该项目的配置权限,请联系管理员开通");
        }

        // valid group
        ConfProject group = xxlConfProjectDao.load(xxlConfNode.getAppname());
        if (group==null) {
            return new Result<String>(500, "AppName非法");
        }

        // valid env
        if (StringUtils.isBlank(xxlConfNode.getEnv())) {
            return new Result<String>(500, "配置Env不可为空");
        }
        ConfEnv xxlConfEnv = xxlConfEnvDao.load(xxlConfNode.getEnv());
        if (xxlConfEnv == null) {
            return new Result<String>(500, "配置Env非法");
        }

        // valid key
        if (StringUtils.isBlank(xxlConfNode.getKey())) {
            return new Result<String>(500, "配置Key不可为空");
        }
        xxlConfNode.setKey(xxlConfNode.getKey().trim());

        ConfNode existNode = xxlConfNodeDao.load(xxlConfNode.getEnv(), xxlConfNode.getKey());
        if (existNode != null) {
            return new Result<String>(500, "配置Key已存在，不可重复添加");
        }
        if (!xxlConfNode.getKey().startsWith(xxlConfNode.getAppname())) {
            return new Result<String>(500, "配置Key格式非法");
        }

        // valid title
        if (StringUtils.isBlank(xxlConfNode.getTitle())) {
            return new Result<String>(500, "配置描述不可为空");
        }

        // value force null to ""
        if (xxlConfNode.getValue() == null) {
            xxlConfNode.setValue("");
        }

        // add node
        //xxlConfZKManager.set(xxlConfNode.getEnv(), xxlConfNode.getKey(), xxlConfNode.getValue());
        xxlConfNodeDao.insert(xxlConfNode);

        // node log
        ConfNodeLog nodeLog = new ConfNodeLog();
        nodeLog.setEnv(xxlConfNode.getEnv());
        nodeLog.setKey(xxlConfNode.getKey());
        nodeLog.setTitle(xxlConfNode.getTitle() + "(配置新增)" );
        nodeLog.setValue(xxlConfNode.getValue());
        nodeLog.setOptuser(loginUser.getUsername());
        xxlConfNodeLogDao.add(nodeLog);

        // conf msg
        sendConfMsg(xxlConfNode.getEnv(), xxlConfNode.getKey(), xxlConfNode.getValue());

        return Result.SUCCESS;
    }

    @Override
    public Result<String> update(ConfNode xxlConfNode, ConfUser loginUser, String loginEnv) {

        // valid
        if (StringUtils.isBlank(xxlConfNode.getKey())) {
            return new Result<String>(500, "配置Key不可为空");
        }
        ConfNode existNode = xxlConfNodeDao.load(xxlConfNode.getEnv(), xxlConfNode.getKey());
        if (existNode == null) {
            return new Result<String>(500, "配置Key非法");
        }

        // project permission
        if (!ifHasProjectPermission(loginUser, loginEnv, existNode.getAppname())) {
            return new Result<String>(500, "您没有该项目的配置权限,请联系管理员开通");
        }

        if (StringUtils.isBlank(xxlConfNode.getTitle())) {
            return new Result<String>(500, "配置描述不可为空");
        }

        // value force null to ""
        if (xxlConfNode.getValue() == null) {
            xxlConfNode.setValue("");
        }

        // update conf
        //xxlConfZKManager.set(xxlConfNode.getEnv(), xxlConfNode.getKey(), xxlConfNode.getValue());

        existNode.setTitle(xxlConfNode.getTitle());
        existNode.setValue(xxlConfNode.getValue());
        int ret = xxlConfNodeDao.update(existNode);
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
        xxlConfNodeLogDao.add(nodeLog);
        xxlConfNodeLogDao.deleteTimeout(existNode.getEnv(), existNode.getKey(), 10);

        // conf msg
        sendConfMsg(xxlConfNode.getEnv(), xxlConfNode.getKey(), xxlConfNode.getValue());

        return Result.SUCCESS;
    }

	/*@Override
	public Result<String> syncConf(String appname, ConfUser loginUser, String loginEnv) {

		// valid
		ConfEnv xxlConfEnv = xxlConfEnvDao.load(loginEnv);
		if (xxlConfEnv == null) {
			return new Result<String>(500, "配置Env非法");
		}
		ConfProject group = xxlConfProjectDao.load(appname);
		if (group==null) {
			return new Result<String>(500, "AppName非法");
		}

		// project permission
		if (!ifHasProjectPermission(loginUser, loginEnv, appname)) {
			return new Result<String>(500, "您没有该项目的配置权限,请联系管理员开通");
		}

		List<ConfNode> confNodeList = xxlConfNodeDao.pageList(0, 10000, loginEnv, appname, null);
		if (CollectionUtils.isEmpty(confNodeList)) {
			return new Result<String>(500, "操作失败，该项目下不存在配置项");
		}

		// un sync node
		List<ConfNode> unSyncConfNodeList = new ArrayList<>();
		for (ConfNode node: confNodeList) {
			String realNodeValue = xxlConfZKManager.get(node.getEnv(), node.getKey());
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

			xxlConfZKManager.set(node.getEnv(), node.getKey(), node.getValue());

			// node log
			ConfNodeLog nodeLog = new ConfNodeLog();
			nodeLog.setEnv(node.getEnv());
			nodeLog.setKey(node.getKey());
			nodeLog.setTitle(node.getTitle() + "(全量同步)" );
			nodeLog.setValue(node.getValue());
			nodeLog.setOptuser(loginUser.getUsername());
			xxlConfNodeLogDao.add(nodeLog);
			xxlConfNodeLogDao.deleteTimeout(node.getEnv(), node.getKey(), 10);

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
                        List<ConfNodeMsg> messageList = xxlConfNodeMsgDao.findMsg(readedMessageIds);
                        if (messageList!=null && messageList.size()>0) {
                            for (ConfNodeMsg message: messageList) {
                                readedMessageIds.add(message.getId());


                                // sync file
                                setFileConfData(message.getEnv(), message.getKey(), message.getValue());
                            }
                        }

                        // clean old message;
                        if ( (System.currentTimeMillis()/1000) % confBeatTime ==0) {
                            xxlConfNodeMsgDao.cleanMessage(confBeatTime);
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

                        List<ConfNode> confNodeList = xxlConfNodeDao.pageList(offset, pagesize, null, null, null);
                        while (confNodeList!=null && confNodeList.size()>0) {

                            for (ConfNode confNoteItem: confNodeList) {

                                // sync file
                                String confDataFile = setFileConfData(confNoteItem.getEnv(), confNoteItem.getKey(), confNoteItem.getValue());

                                // collect confDataFile
                                confDataFileList.add(confDataFile);
                            }


                            offset += 1000;
                            confNodeList = xxlConfNodeDao.pageList(offset, pagesize, null, null, null);
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
