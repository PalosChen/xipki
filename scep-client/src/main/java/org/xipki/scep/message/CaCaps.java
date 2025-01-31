/*
 *
 * Copyright (c) 2013 - 2020 Lijun Liao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.xipki.scep.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xipki.scep.transaction.CaCapability;
import org.xipki.security.HashAlgo;
import org.xipki.util.Args;
import org.xipki.util.CollectionUtil;
import org.xipki.util.StringUtil;

import java.util.*;

/**
 * CA caps.
 *
 * @author Lijun Liao
 */

public class CaCaps {

  private static final Logger LOG = LoggerFactory.getLogger(CaCaps.class);

  private byte[] bytes;

  private final Set<CaCapability> capabilities;

  public CaCaps() {
    this.capabilities = new HashSet<>();
  }

  public CaCaps(Set<CaCapability> capabilities) {
    this.capabilities = CollectionUtil.isEmpty(capabilities)
        ? new HashSet<>() : new HashSet<>(capabilities);
    refresh();
  }

  public Set<CaCapability> capabilities() {
    return Collections.unmodifiableSet(capabilities);
  }

  public void addCapabilities(CaCapability... caps) {
    Args.notNull(caps, "caps");
    Collections.addAll(capabilities, caps);
    refresh();
  }

  public void removeCapabilities(CaCaps caCaps) {
    Args.notNull(caCaps, "caCaps");
    this.capabilities.retainAll(caCaps.capabilities);
    refresh();
  }

  public void removeCapabilities(CaCapability... caps) {
    Args.notNull(caps, "caps");
    for (CaCapability m : caps) {
      capabilities.remove(m);
    }
    refresh();
  }

  public boolean containsCapability(CaCapability cap) {
    Args.notNull(cap, "cap");
    return capabilities.contains(cap);
  }

  @Override
  public String toString() {
    return toScepMessage();
  }

  @Override
  public int hashCode() {
    return toScepMessage().hashCode();
  }

  public String toScepMessage() {
    if (capabilities.isEmpty()) {
      return "";
    }

    StringBuilder sb = new StringBuilder();
    for (CaCapability cap : capabilities) {
      sb.append(cap.getText()).append("\n");
    }
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }

  public boolean supportsPost() {
    return capabilities.contains(CaCapability.POSTPKIOperation);
  }

  public HashAlgo mostSecureHashAlgo() {
    if (capabilities.contains(CaCapability.SHA512)) {
      return HashAlgo.SHA512;
    } else if (capabilities.contains(CaCapability.SHA256)) {
      return HashAlgo.SHA256;
    } else {
      return HashAlgo.SHA1;
    }
  }

  private void refresh() {
    if (capabilities != null) {
      this.bytes = StringUtil.toUtf8Bytes(toString());
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof CaCaps)) {
      return false;
    }

    return capabilities.equals(((CaCaps) obj).capabilities);
  }

  public byte[] getBytes() {
    return Arrays.copyOf(bytes, bytes.length);
  }

  public static CaCaps getInstance(String scepMessage) {
    CaCaps ret = new CaCaps();
    if (StringUtil.isBlank(scepMessage)) {
      return ret;
    }

    StringTokenizer st = new StringTokenizer(scepMessage, "\r\n");

    List<CaCapability> caps = new ArrayList<>(st.countTokens());
    while (st.hasMoreTokens()) {
      String token = st.nextToken();
      try {
        caps.add(CaCapability.forValue(token));
      } catch (IllegalArgumentException ex) {
        LOG.warn("ignore unknown CACap '{}'", token);
      }
    }

    if (!caps.isEmpty()) {
      ret.addCapabilities(caps.toArray(new CaCapability[0]));
    }

    return ret;
  } // method getInstance

}
