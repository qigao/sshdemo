package com.crscd.ssh;

import java.io.*;
import java.util.concurrent.*;

import net.schmizz.sshj.*;
import net.schmizz.sshj.connection.channel.direct.*;
import net.schmizz.sshj.userauth.method.AuthMethod;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.xfer.FileSystemFile;

import java.io.IOException;
import java.net.InetSocketAddress;
/**
 * Java SSHv2 Client Demo
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException,InterruptedException
    {
        if (args.length ==0){
            System.err.println("pls connect to a host like this: qi@192.168.1.100");
            System.exit(1);
        }
        String target = args[0];
         
        String username = target.substring(0, target.indexOf('@'));
        String hostname = target.substring(target.indexOf('@') + 1);

        final SSHClient client = new SSHClient();
        client.addHostKeyVerifier(new PromiscuousVerifier());
        client.useCompression();
        client.connect(hostname);

        try{
            // auth by key, this key should also be found at server side
            // like: /home/user/.ssh/authorized_key
            client.authPublickey(username,"/home/qigao/.ssh/id_rsa");
            final Session session = client.startSession();
            //execute command
            try{
                // put your command here
                final Session.Command cmd = session.exec("ps -ef|grep \"/usr/sbin/sshd\"");
                cmd.join(5,TimeUnit.SECONDS);
                BufferedReader reader = new BufferedReader(new InputStreamReader(cmd.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }finally{
                session.close();
            }
            
            // download file
            final SFTPClient sftp = client.newSFTPClient();
            try {
                sftp.get("/root/install.log",new FileSystemFile("."));
            }finally{
                sftp.close();
            }
            
        }finally{
            client.disconnect();
        }
    }
}
