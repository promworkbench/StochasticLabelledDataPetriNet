package org.processmining.stochasticlabelleddatapetrinets;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.processmining.contexts.cli.CLI;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.boot.Boot.Level;
import org.processmining.framework.packages.PackageManager;
import org.processmining.framework.plugin.PluginManager;
import org.processmining.framework.plugin.impl.PluginManagerImpl;

public class IntegrationTestUtil {

	public static PluginManager initializeProMWithRequiredPackages(String... packageNames) throws Throwable {
		Boot.VERBOSE = Level.ALL;
		PackageManager.getInstance().initialize(Level.NONE);
		PackageManager.getInstance().findOrInstallPackages(packageNames);
		PrintStream err = System.err;
		System.setErr(new PrintStream(new OutputStream() {
			
			public void write(int b) throws IOException {
				// dont show
			}
		}));
		CLI.main(new String[] {});
		System.loadLibrary("lpsolve55");
		System.loadLibrary("lpsolve55j");
		System.setErr(err);
		return PluginManagerImpl.getInstance();
	}

}
