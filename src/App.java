/**
 * Copyright (c) 2013 William Harding and Jonathan McCluskey.
 *
 * Permission is hereby granted, free of charge, to any person 
 * obtaining a copy of this software and associated documentation 
 * files (the "Software"), to deal in the Software without 
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be 
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE. 
 *
 * @author Jonathan McCluskey <jonathan.m.mccluskey@gmail.com>
 */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Text;

import thep.paillier.*;
import thep.paillier.exceptions.BigIntegerClassNotValid;
import thep.paillier.exceptions.PublicKeysNotEqualException;

public class App 
{
    private  BigInteger p1x;
    private  BigInteger p1y;
    private  BigInteger p2x;
    private  BigInteger p2y;

    private  List<Integer> result = new ArrayList<>();
            
    private  ObjectOutputStream sOut;
    private  ObjectInputStream sIn;

    private  PrivateKey priv; 
    private  PublicKey  pub;
    
    private  PublicKey theirPub;

    public App(Integer port, String ipAddress, Point p1, Point p2, Text status) {
        p1x = new BigInteger(Integer.toString(p1.x));
        p1y = new BigInteger(Integer.toString(p1.y)); 
        p2x = new BigInteger(Integer.toString(p2.x));
        p2y = new BigInteger(Integer.toString(p2.y)); 
    
        priv = new PrivateKey(1024);
        pub = priv.getPublicKey();

        try {
            Socket socket = new Socket();
            SocketAddress bindAddr = new InetSocketAddress(ipAddress, port);
            
            Boolean leader = false;

            try {
                // Try connecting
                socket.connect(bindAddr);
                leader = false;
            } catch (ConnectException e) {
                // If connection fails, the just listen for a connection
                ServerSocket ss = new ServerSocket(port);
                socket = ss.accept();
                ss.close();
                leader = true;
            }

            sOut = new ObjectOutputStream(socket.getOutputStream());
            sIn  = new ObjectInputStream(socket.getInputStream());
            
            // Share our public key
            sOut.writeObject(pub);
            theirPub = (PublicKey)sIn.readObject();
            
            if (leader) {
                readPointsWriteResults();
                writePointsReadResults();
            } else {
                writePointsReadResults();
                readPointsWriteResults();
            }
            
            Boolean myResult = intersect();
            sOut.writeObject(myResult);
            sOut.flush();

            Boolean theirResult = (Boolean)sIn.readObject();
            if (myResult.equals(theirResult)) {
                System.out.println(myResult ? "We Intersect." : "We Don't Intersect.");
                status.setText(myResult ? "We Intersect." : "We Don't Intersect.");
            } else {
                System.out.println("We Don't Agree! I say: " + 
                                   (myResult ? "We Intersect." : "We Don't Intersect.") + " They Say: " +
                                   (theirResult ? "We Intersect." : "We Don't Intersect."));
                status.setText("We Don't Agree! I say: " + 
                               (myResult ? "We Intersect." : "We Don't Intersect.") + " They Say: " +
                               (theirResult ? "We Intersect." : "We Don't Intersect."));
            }

            socket.close();

        } catch (ClassNotFoundException | IOException e) {
        } finally {
        }
    }

    //    Integer result = (p2.y * p1.x) - (p1.y * p2.x) - (p.x * p2.y) + 
    //                     (p.x  * p1.y) + (p.y  * p2.x) - (p.y * p1.x);
    private  EncryptedInteger order(EncryptedInteger px, EncryptedInteger py) {
        try {
            return (px.multiply(p2y)).multiply(new BigInteger("-1")).add(
                    (p2y.multiply(p1x).subtract(p1y.multiply(p2x)))).add( 
                        (px.multiply(p1y))).add(
                            (py.multiply(p2x))).add(
                                (py.multiply(p1x)).multiply(new BigInteger("-1")));
        } catch (BigIntegerClassNotValid | PublicKeysNotEqualException e) {
            return null;
        }
    }
    
    private  Boolean intersect() {
        return (! ((((result.get(0) > 0) && (result.get(1) > 0))  || 
                    ((result.get(0) < 0) && (result.get(1) < 0))) ||
                   (((result.get(2) > 0) && (result.get(3) > 0))  || 
                    ((result.get(2) < 0) && (result.get(3) < 0)))));
    }

    private  void writeObjectsOut(Object x1, Object x2) {
        try {
            sOut.writeObject(x1);
            sOut.writeObject(x2);
            sOut.flush();
        } catch (IOException e) {
        }
    }

    private  void readPointsWriteResults() {
        try {
            // Read in their encrypted points, encrypted with their public key
            EncryptedInteger x1 = (EncryptedInteger)sIn.readObject();
            EncryptedInteger y1 = (EncryptedInteger)sIn.readObject();
            EncryptedInteger x2 = (EncryptedInteger)sIn.readObject();
            EncryptedInteger y2 = (EncryptedInteger)sIn.readObject();

            // These results are encrypted with their public key
            EncryptedInteger r1 = order(x1, y1);
            EncryptedInteger r2 = order(x2, y2);

            // Write out the results that are encrypted with their public key
            writeObjectsOut(r1, r2); 

            // Read in the results encrypted with our public key
            Integer pr1 =  getNextResult();
            Integer pr2 =  getNextResult();

            // Add the decrypted results
            result.add(pr1);
            result.add(pr2);
        } catch (IOException | ClassNotFoundException e) {
        }
    }

    private  void writePointsReadResults() {
        try {
            // Write out our points, encrypted with our public key
            writeObjectsOut(new EncryptedInteger(p1x,pub), new EncryptedInteger(p1y,pub)); 
            writeObjectsOut(new EncryptedInteger(p2x,pub), new EncryptedInteger(p2y,pub)); 

            Integer intPr1 = getNextResult();
            Integer intPr2 = getNextResult();
            
            result.add(intPr1);
            result.add(intPr2);

            //Encrypt with their public key and write the results
            writeObjectsOut(new EncryptedInteger(new BigInteger(intPr1.toString()), theirPub), 
            		            new EncryptedInteger(new BigInteger(intPr2.toString()) ,theirPub)); 
        } catch (BigIntegerClassNotValid e) {
        }
    }

    private  Boolean isNegative(BigInteger i) {
        //TODO(WPH): What should we compare to?
        return i.compareTo(new BigInteger("100000000")) == 1; 
    }

    private  Integer getNextResult() {
        Integer prs = 0;

        try {
            // These results, from them, are encrypted with our public key
            EncryptedInteger r =  (EncryptedInteger)sIn.readObject();

            // Decrypt the results
            BigInteger pr = r.decrypt(priv);
            if (isNegative(pr)) {
                pr = pr.subtract(pub.getN()); // subtract N since answer is neg
            }
            
            prs = pr.signum();
        } catch (IOException | ClassNotFoundException | BigIntegerClassNotValid e) {
        }

        return prs;
    }
}
