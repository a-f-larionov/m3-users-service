package m3.users.services;

import m3.users.enums.SocNetType;

public interface SocNetSwitcher {
    SocNetService getByType(SocNetType socNetType);
}