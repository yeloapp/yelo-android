/*
 *
 *  * Copyright (C) 2015 yelo.red
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package red.yelo.utils.md5;

class MD5State {
  /**
   * 128-bit state 
   */
  int	state[];
  
  /**
   * 64-bit character count
   */
  long count;
  
  /**
   * 64-byte buffer (512 bits) for storing to-be-hashed characters
   */
  byte	buffer[];

  public MD5State() {
    buffer = new byte[64];
    count = 0;
    state = new int[4];
    
    state[0] = 0x67452301;
    state[1] = 0xefcdab89;
    state[2] = 0x98badcfe;
    state[3] = 0x10325476;

  }

  /** Create this State as a copy of another state */
  public MD5State (MD5State from) {
    this();
    
    int i;
    
    for (i = 0; i < buffer.length; i++)
      this.buffer[i] = from.buffer[i];
    
    for (i = 0; i < state.length; i++)
      this.state[i] = from.state[i];
    
    this.count = from.count;
  }
};
