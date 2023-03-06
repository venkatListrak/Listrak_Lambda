package com.util;



import com.jobs.NgListrakOrderExport;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import static com.squareup.okhttp.internal.Internal.logger;

public class SFTPClient {

	private Session session = null;

	public void connect() {
		try {
			JSch jsch = new JSch();
			session = jsch.getSession(NgListrakOrderExport.SFTP_USERNAME, NgListrakOrderExport.SFTP_HOST,
					Integer.valueOf(NgListrakOrderExport.SFTP_PORT));
			session.setPassword(NgListrakOrderExport.SFTP_PASSWORD);
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();
		}
		catch (Exception e){
			logger.info("Failed to establish sftp connection:  " + e.getMessage());
		}
	}

	public void upload (String source, String destination)  {
		try {
			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;
			sftpChannel.put(source, destination);
			sftpChannel.exit();
		} catch (Exception e) {
			logger.info("Failed to upload to destination:  " + e.getMessage());
		}
	}

	public void disconnect() {
		if (session != null) {
			session.disconnect();
		}
	}
}