/*
 * Copyright (C) 2009 The Android Open Source Project
 * Copyright 2014 gitblit.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tshank.sshproxy.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.session.ServerSession;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.util.SystemReader;
import org.tshank.sshproxy.App;
import org.tshank.sshproxy.ssh.IPublicKeyManager;
import org.tshank.sshproxy.ssh.SshDaemonClient;


/**
 * Class that displays a welcome message for any shell requests.
 *
 */
public class WelcomeShell implements Factory<Command> {

	private final App app;

	public WelcomeShell(App app) {
		this.app = app;
	}

	@Override
	public Command create() {
		return new SendMessage(app);
	}

	private static class SendMessage implements Command, SessionAware {

		private final IPublicKeyManager km;
		private ServerSession session;

		private InputStream in;
		private OutputStream out;
		private OutputStream err;
		private ExitCallback exit;

		SendMessage(App app) {
			this.km = app.getPublicKeyManager();
		}

		@Override
		public void setInputStream(final InputStream in) {
			this.in = in;
		}

		@Override
		public void setOutputStream(final OutputStream out) {
			this.out = out;
		}

		@Override
		public void setErrorStream(final OutputStream err) {
			this.err = err;
		}

		@Override
		public void setExitCallback(final ExitCallback callback) {
			this.exit = callback;
		}

		@Override
		public void setSession(final ServerSession session) {
			this.session = session;
		}

		@Override
		public void start(final Environment env) throws IOException {
			err.write(Constants.encode(getMessage()));
			err.flush();

			in.close();
			out.close();
			err.close();
			exit.onExit(127);
		}

		@Override
		public void destroy() {
			this.session = null;
		}

		String getMessage() {
			return "AAAAAAAAAAAA";
		}
	}
}
