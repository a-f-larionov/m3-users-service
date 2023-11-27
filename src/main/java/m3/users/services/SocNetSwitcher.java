package m3.users.services;

import m3.lib.enums.SocNetType;

public interface SocNetSwitcher {
    SocNetService getByType(SocNetType socNetType);
}