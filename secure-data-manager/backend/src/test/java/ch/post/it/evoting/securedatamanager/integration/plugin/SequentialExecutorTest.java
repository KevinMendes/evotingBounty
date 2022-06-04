/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.integration.plugin;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

/**
 * This test class focuses on the Windows operating system.
 **/
class SequentialExecutorTest {

	private final ExecutionListener listener = new ExecutionListener();
	private final SequentialExecutor sequentialExecutor = new SequentialExecutor();

	private PluginSequenceResolver commands;
	private Parameters parameters;

	@BeforeEach
	public void setEnv() throws IOException, JAXBException, SAXException, URISyntaxException, XMLStreamException {
		parameters = new Parameters();
		parameters.addParam(KeyParameter.EE_ALIAS.name(), "EE_159_tiebreaks");
		parameters.addParam(KeyParameter.EE_ID.name(), "8f057477a28e425ea8a70321807bf126");
		parameters.addParam(KeyParameter.SDM_PATH.name(), "C:\\\\Users\\\\xyzxyz\\\\sdm");
		parameters.addParam(KeyParameter.USB_LETTER.name(), "D:");
		parameters.addParam(KeyParameter.PRIVATE_KEY.name(), "asdadad");

		final Plugins plugins = XmlObjectsLoader.loadFile("/plugins_test_err.xml");
		commands = new PluginSequenceResolver(plugins);
	}

	@Test
	void execute_command_test() {
		final List<String> commandsForPhase = commands.getActionsForPhaseAndOrder(PhaseName.GENERATE_PRE_VOTING_OUTPUTS.value(), 1);
		assertDoesNotThrow(() -> sequentialExecutor.execute(commandsForPhase, parameters, listener));
	}

	@Test
	void when_command_invalid_action_then_error_101_test() throws Exception {
		final List<String> commandsForPhase = commands.getActionsForPhaseAndOrder(PhaseName.GENERATE_PRE_VOTING_OUTPUTS.value(), 1);
		invoke(ResultCode.INVALID_ACTION, commandsForPhase);
	}

	@Test
	void when_command_missing_params_then_error_102_test() throws Exception {
		final List<String> commandsForPhase = commands.getActionsForPhaseAndOrder(PhaseName.GENERATE_PRE_VOTING_OUTPUTS.value(), 2);
		invoke(ResultCode.MISSING_PARAMETER, commandsForPhase);
	}

	@Test
	void when_command_param_invalid_then_error_103_test() throws Exception {
		final List<String> commandsForPhase = commands.getActionsForPhaseAndOrder(PhaseName.GENERATE_PRE_VOTING_OUTPUTS.value(), 3);
		invoke(ResultCode.PARAMETER_NOT_VALID, commandsForPhase);
	}

	@Test
	void when_command_filenotfound_then_error_104_test() throws Exception {
		final List<String> commandsForPhase = commands.getActionsForPhaseAndOrder(PhaseName.GENERATE_PRE_VOTING_OUTPUTS.value(), 4);
		invoke(ResultCode.FILE_NOT_FOUND, commandsForPhase);
	}

	@Test
	void when_error_parsing_then_error_105_test() throws Exception {
		final List<String> commandsForPhase = commands.getActionsForPhaseAndOrder(PhaseName.GENERATE_PRE_VOTING_OUTPUTS.value(), 5);
		invoke(ResultCode.ERROR_PARSING_FILE, commandsForPhase);
	}

	@Test
	void executeCommandTest() throws URISyntaxException, IOException, JAXBException, SAXException, XMLStreamException {

		final URI uri = this.getClass().getResource("/validPlugin.xml").toURI();
		final String resourcePath = uri.getPath();
		final Plugins plugins = XmlObjectsLoader.unmarshal(new File(resourcePath).toPath());

		final PluginSequenceResolver pluginSequence = new PluginSequenceResolver(plugins);
		final List<String> commandsForPhase = pluginSequence.getActionsForPhase(PhaseName.GENERATE_PRE_VOTING_OUTPUTS);

		final Parameters parameters = new Parameters();
		parameters.addParam(KeyParameter.EE_ALIAS.name(), "EE_159_tiebreaks");
		parameters.addParam(KeyParameter.EE_ID.name(), "8f057477a28e425ea8a70321807bf126");
		parameters.addParam(KeyParameter.SDM_PATH.name(), Paths.get(uri).getParent().toString());
		parameters.addParam(KeyParameter.USB_LETTER.name(), "D:");

		invoke(ResultCode.SUCCESS, commandsForPhase);
	}

	private void invoke(final ResultCode errorCode, List<String> commandsForPhase) throws URISyntaxException {
		if (!System.getProperty("os.name").toLowerCase().contains("win")) {
			return;
		}
		commandsForPhase = sanitize(commandsForPhase, errorCode);
		sequentialExecutor.execute(commandsForPhase, parameters, listener);
		if (ResultCode.SUCCESS.equals(errorCode)) {
			assertEquals(0, listener.getError());
		} else {
			assertEquals(errorCode.value(), listener.getError());
		}
	}

	private List<String> sanitize(final List<String> commandsForPhase, final ResultCode errorCode) throws URISyntaxException {
		final List<String> sanitized = new ArrayList<>();
		for (final String string : commandsForPhase) {
			sanitized.add(sanitize(string, errorCode));
		}
		return sanitized;
	}

	private String sanitize(final String string, final ResultCode errorCode) throws URISyntaxException {
		String absolutePath = "";
		if (ResultCode.SUCCESS.equals(errorCode)) {
			absolutePath = Paths.get(this.getClass().getResource("/success" + errorCode.value() + ".bat").toURI()).toFile().getAbsolutePath();
		} else {
			absolutePath = Paths.get(this.getClass().getResource("/error" + errorCode.value() + ".bat").toURI()).toFile().getAbsolutePath();
		}
		return absolutePath;
	}
}
