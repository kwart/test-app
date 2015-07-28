package org.jboss.test;

import java.util.Set;

import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.logging.log4j.config.Log4JLoggingModule;
import org.jclouds.sshj.config.SshjSshClientModule;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

/**
 * Hello world!
 *
 * @author Josef Cacek
 */
public class App {

	public static void main(String[] args) throws RunNodesException {

		// get a context with docker that offers the portable ComputeService api
		ComputeServiceContext context = ContextBuilder.newBuilder("docker").credentials("root", "redhat")
				.modules(ImmutableSet.<Module> of(new Log4JLoggingModule(), new SshjSshClientModule()))
				.buildView(ComputeServiceContext.class);
		ComputeService client = context.getComputeService();

		String sshableImageId = "536ab7760fcdfa11b5325042f5d4f9ac88bae5744e10d992c36a5d9534fafc4c"; // this can be obtained using
													// `docker images
													// --no-trunc` command
		Template template = client.templateBuilder().imageId(sshableImageId).build();

		// run a couple nodes accessible via group container
		Set<? extends NodeMetadata> nodes = client.createNodesInGroup("container", 2, template);

		nodes.forEach(NodeMetadata::toString);

		// release resources
		context.close();
	}

}
