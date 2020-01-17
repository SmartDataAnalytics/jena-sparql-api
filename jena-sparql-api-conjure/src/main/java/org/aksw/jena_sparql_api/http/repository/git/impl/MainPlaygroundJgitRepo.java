package org.aksw.jena_sparql_api.http.repository.git.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.util.FS;

import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.Connector;
import com.jcraft.jsch.agentproxy.RemoteIdentityRepository;
import com.jcraft.jsch.agentproxy.USocketFactory;
import com.jcraft.jsch.agentproxy.connector.SSHAgentConnector;
import com.jcraft.jsch.agentproxy.usocket.JNAUSocketFactory;

public class MainPlaygroundJgitRepo {
	public static List<Path> listPrivateKeys(Path path) throws IOException {
		List<Path> result = Files.list(path)
				//.peek(p -> System.out.println(p))
				//.filter(p -> p.getFileName().toString().endsWith(".pub"))
				.filter(p -> !p.getFileName().toAbsolutePath().endsWith(".pub"))
				.filter(p -> Files.exists(p.getParent().resolve(p.getFileName().toString() + ".pub")))
				.collect(Collectors.toList());
		System.out.println(result);

		return result;
	}

	public static void main(String[] args) throws Exception {
		SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {

//			@Override
//			protected JSch createDefaultJSch(FS fs) throws JSchException {
//				JSch defaultJSch = super.createDefaultJSch(fs);
//				//defaultJSch.removeAllIdentity();
//
//				Path path = Paths.get(StandardSystemProperty.USER_HOME.value()).resolve(".ssh");
//
//				List<Path> privateKeyFiles;
//				try {
//					privateKeyFiles = listPrivateKeys(path);
//				} catch (IOException e) {
//					throw new RuntimeException(e);
//				}
//
//				for (Path p : privateKeyFiles) {
//					defaultJSch.addIdentity(p.toString());
//				}
//				return defaultJSch;
//			}

			@Override
			protected JSch createDefaultJSch(FS fs) throws JSchException {
		        Connector con = null;
		        try {
		            if(SSHAgentConnector.isConnectorAvailable()){
		                //USocketFactory usf = new JUnixDomainSocketFactory();
		                USocketFactory usf = new JNAUSocketFactory();
		                con = new SSHAgentConnector(usf);
		            }
		        } catch(AgentProxyException e){
		            System.out.println(e);
		        }

		        JSch jsch = super.createDefaultJSch(fs);
		        if (con != null) {
		            JSch.setConfig("PreferredAuthentications", "publickey");

		            IdentityRepository identityRepository = new RemoteIdentityRepository(con);
		            jsch.setIdentityRepository(identityRepository);
		        }
		        
		        
		        return jsch;
//		        if (con == null) {
//		            return 
//		        } else {
//		            final JSch jsch = new JSch();
//		            jsch.setConfig("PreferredAuthentications", "publickey");
//		            IdentityRepository irepo = new RemoteIdentityRepository(con);
//		            jsch.setIdentityRepository(irepo);
//		            knownHosts(jsch, fs); // private method from parent class, yeah for Groovy!
//		            return jsch;
//		        }
		    }
		

			@Override
			protected void configure(OpenSshConfig.Host host, Session session) {

				session.setConfig("StrictHostKeyChecking", "no");
			
				session.setUserInfo(new UserInfo() {
					@Override
					public String getPassphrase() {
						System.err.println("Passphrase requested");
						return null;
						//return "passphrase";
					}

					@Override
					public String getPassword() {
						return null;
					}

					@Override
					public boolean promptPassword(String message) {
						System.err.println(message);
						return true;
					}

					@Override
					public boolean promptPassphrase(String message) {
						System.err.println(message);
						return true;
					}

					@Override
					public boolean promptYesNo(String message) {
						System.err.println(message);
						return true;
					}

					@Override
					public void showMessage(String message) {
						System.err.println(message);
					}
				});

			}

		};

		Git gitRepo = Git.open(new File("/home/raven/Projects/limbo/git/metadata-catalog"));

		PullResult res = gitRepo.pull().setTransportConfigCallback(transport -> {
			SshTransport sshTransport = (SshTransport) transport;
			sshTransport.setSshSessionFactory(sshSessionFactory);
		}).call();
		System.out.println(res);
	}
}
