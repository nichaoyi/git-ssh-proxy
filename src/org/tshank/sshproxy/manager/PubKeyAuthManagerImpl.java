package org.tshank.sshproxy.manager;

import org.tshank.sshproxy.models.UserModel;
import org.tshank.sshproxy.ssh.SshKey;
import org.tshank.sshproxy.utils.StringUtils;

public class PubKeyAuthManagerImpl implements IAuthenticationManager {

  @Override
  public UserModel authenticate(String username, SshKey key) {
    if (username != null) {
      if (!StringUtils.isEmpty(username)) {
         return new UserModel(username);
      }
    }
    return null;
  }
}
