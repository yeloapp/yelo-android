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
 */package red.yelo.utils.md5;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MD5OutputStream extends FilterOutputStream {
    /**
     * MD5 context
     */
    private MD5	md5;

    /**
     * Creates MD5OutputStream
     * @param out	The output stream
     */

    public MD5OutputStream (OutputStream out) {
	super(out);

	md5 = new MD5();
    }

    /**
     * Writes a byte. 
     *
     * @see java.io.FilterOutputStream
     */

    public void write (int b) throws IOException {
	out.write(b);
	md5.Update((byte) b);
    }

    /**
     * Writes a sub array of bytes.
     *
     * @see java.io.FilterOutputStream
     */

    public void write (byte b[], int off, int len) throws IOException {
	out.write(b, off, len);
	md5.Update(b, off, len);
    }

    /**
     * Returns array of bytes representing hash of the stream as finalized
     * for the current state.
     * @see MD5#Final
     */

    public byte[] hash () {
	return md5.Final();
    }

  public MD5 getMD5() {
    return md5;
  }

  /**
   * This method is here for testing purposes only - do not rely
   * on it being here.
   **/
  public static void main(String[] arg) {
    try {
      MD5OutputStream out = new MD5OutputStream(new NullOutputStream());
      InputStream in = new BufferedInputStream(new FileInputStream(arg[0]));
      byte[] buf = new byte[65536];
      int num_read;
      long total_read = 0;
      while ((num_read = in.read(buf)) != -1) {
	total_read += num_read;
	out.write(buf, 0, num_read);
      }
      System.out.println(MD5.asHex(out.hash())+"  "+arg[0]);
      in.close();
      out.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


}

