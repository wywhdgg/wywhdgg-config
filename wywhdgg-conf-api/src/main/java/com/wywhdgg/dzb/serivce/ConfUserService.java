package com.wywhdgg.dzb.serivce;

import com.wywhdgg.dzb.entity.ConfUser;
import java.util.List;

public interface ConfUserService {
    public ConfUser findNameByConfUser(String usernameParam);

    public List<ConfUser> pageList(int offset, int pagesize, String username, int permission);

    public int pageListCount(int offset, int pagesize, String username, int permission);

    public int add(ConfUser confUser);

    public int update(ConfUser confUser);

    public int delete(String username);

    public ConfUser load(String username);
}
