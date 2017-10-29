/*
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tshank.sshproxy.git.GitDispatcher;
import org.tshank.sshproxy.ssh.SshDaemonClient;
import org.tshank.sshproxy.utils.WorkQueue;


/**
 * The root dispatcher is the dispatch command that handles registering all
 * other commands.
 *
 */
@CommandMetaData(name = "")
class RootDispatcher extends DispatchCommand {

	private Logger log = LoggerFactory.getLogger(getClass());

	public RootDispatcher(SshDaemonClient client, String cmdLine, WorkQueue workQueue) {
		super();
		setContext(new SshCommandContext(client, cmdLine));
		setWorkQueue(workQueue);

		//register(VersionCommand.class);
		register(GitDispatcher.class);
		//register(KeysDispatcher.class);
		//register(PluginDispatcher.class);

		/*List<DispatchCommand> exts = gitblit.getExtensions(DispatchCommand.class);
		for (DispatchCommand ext : exts) {
			Class<? extends DispatchCommand> extClass = ext.getClass();
			PluginWrapper wrapper = gitblit.whichPlugin(extClass);
			String plugin = wrapper.getDescriptor().getPluginId();
			CommandMetaData meta = extClass.getAnnotation(CommandMetaData.class);
			log.debug("Dispatcher {} is loaded from plugin {}", meta.name(), plugin);
			register(ext);
		}*/
	}

	@Override
	protected final void setup() {
	}
}