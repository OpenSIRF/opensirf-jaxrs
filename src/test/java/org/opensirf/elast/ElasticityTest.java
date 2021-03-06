/*
 * OpenSIRF JAX-RS
 * 
 * Copyright IBM Corporation 2016.
 * All Rights Reserved.
 * 
 * MIT License:
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 * Except as contained in this notice, the name of a copyright holder shall not
 * be used in advertising or otherwise to promote the sale, use or other
 * dealings in this Software without prior written authorization of the
 * copyright holder.
 */
package org.opensirf.elast;

import org.junit.Test;
import org.opensirf.elast.controller.ElasticityController;
import org.opensirf.format.GenericUnmarshaller;

/**
 * @author pviana
 *
 */
public class ElasticityTest {	
	@Test
    public void scaleOutMarshallingTest() throws Exception {
    	String jsonScaleOutReq = "{\"containerConfiguration\":{\"containerName\":\"lv1\",\"driver\":\"fs\",\"endpoint\":\"localhost\",\"mountPoint\":\"/var/lib/sirf/storage/lv1\"}}";
    
    	ScaleOutRequest request = GenericUnmarshaller.unmarshal("application/json", jsonScaleOutReq, ScaleOutRequest.class);
    	System.out.println("FINAL CLASS == " + request.getContainerConfiguration().getClass().getName() + " " + request.getContainerConfiguration().getDriver());
    }
	
	@Test
	public void jschTest() throws Exception { 
		String output = new ElasticityController().instantiateNewNode("200.144.189.109", "phillip",
				"swift", "devstacknode-elast1");
		System.out.println("OUTPUT == " + output);
	}
}
