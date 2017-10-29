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
package org.tshank.sshproxy.git;

import org.tshank.sshproxy.command.BaseCommand;
import org.tshank.sshproxy.command.CommandMetaData;
import org.tshank.sshproxy.command.DispatchCommand;
import org.tshank.sshproxy.command.SshCommandContext;
import org.tshank.sshproxy.ssh.SshDaemonClient;

@CommandMetaData(name = "git", description="Git repository commands")
public class GitDispatcher extends DispatchCommand {

	/*protected RepositoryResolver<SshDaemonClient> repositoryResolver;
	protected GitblitUploadPackFactory<SshDaemonClient> uploadPackFactory;
	protected GitblitReceivePackFactory<SshDaemonClient> receivePackFactory;*/

	@Override
	public void setContext(SshCommandContext context) {
		super.setContext(context);

		/*IGitblit gitblit = context.getGitblit();
		repositoryResolver = new RepositoryResolver<SshDaemonClient>(gitblit);
		uploadPackFactory = new GitblitUploadPackFactory<SshDaemonClient>(gitblit);
		receivePackFactory = new GitblitReceivePackFactory<SshDaemonClient>(gitblit);*/
	}

	@Override
	public void destroy() {
		super.destroy();

		/*repositoryResolver = null;
		receivePackFactory = null;
		uploadPackFactory = null;*/
	}

	@Override
	protected void setup() {
		register(Upload.class);
		register(Receive.class);
		register(GarbageCollectionCommand.class);
	}

	@Override
	protected void provideStateTo(final BaseCommand cmd) {
		super.provideStateTo(cmd);

		BaseGitCommand a = (BaseGitCommand) cmd;
		/*a.setRepositoryResolver(repositoryResolver);
		a.setUploadPackFactory(uploadPackFactory);
		a.setReceivePackFactory(receivePackFactory);*/
	}
}
