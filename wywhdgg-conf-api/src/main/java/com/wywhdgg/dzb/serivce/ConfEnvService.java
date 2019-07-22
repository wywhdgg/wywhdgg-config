package com.wywhdgg.dzb.serivce;

import com.wywhdgg.dzb.entity.ConfEnv;
import java.util.List;
import org.jboss.logging.Param;

public interface ConfEnvService {
    public List<ConfEnv> findAll();

    public int save(ConfEnv xxlConfEnv);

    public int update(ConfEnv xxlConfEnv);

    public int delete(String env);

    public ConfEnv load(String env);
}
