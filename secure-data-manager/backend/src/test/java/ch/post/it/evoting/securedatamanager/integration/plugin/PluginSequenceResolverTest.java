/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.integration.plugin;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xml.sax.SAXException;

class PluginSequenceResolverTest {

	private PluginSequenceResolver commands;

	@BeforeEach
	public void init() throws IOException, JAXBException, SAXException, URISyntaxException, XMLStreamException {
		Plugins plugins = XmlObjectsLoader.loadFile("/plugins_CHALLENGE_test_err.xml");
		commands = new PluginSequenceResolver(plugins);
	}

	@Test
	void get_actions_for_phase() throws URISyntaxException, IOException, JAXBException, SAXException, XMLStreamException {

		String resourcePath = this.getClass().getResource("/validPlugin.xml").toURI().getPath();
		Plugins plugins = XmlObjectsLoader.unmarshal(new File(resourcePath).toPath());

		PluginSequenceResolver commands = new PluginSequenceResolver(plugins);
		List<String> actionsForPhase = commands.getActionsForPhase(PhaseName.GENERATE_PRE_VOTING_OUTPUTS);
		assertTrue(actionsForPhase.get(0).contains("java -jar #SDM_PATH#\\eCH\\eCH_file_converter\\ech-file-converter.jar"));
		assertTrue(actionsForPhase.get(0).contains("-action election_event_alias_to_id"));
		assertTrue(actionsForPhase.get(0).contains("-election_export_in #SDM_PATH#\\sdmConfig\\config.json"));
		assertTrue(actionsForPhase.get(0)
				.contains("-output #SDM_PATH#\\eCH\\electionEvents\\#EE_ALIAS#\\output\\election_event_alias_to_id_#EE_ALIAS#.csv"));
		assertTrue(actionsForPhase.get(0).contains("-log #SDM_PATH#\\eCH\\electionEvents\\#EE_ALIAS#\\logs\\eCH_file_converter_#EE_ALIAS#.log"));
	}

	@ParameterizedTest
	@ValueSource(ints = { 11, 12, 13, 14, 15, 16 })
	void commandOrderTest(final int order) {
		List<String> commandsForPhase;
		commandsForPhase = commands.getActionsForPhaseAndOrder(PhaseName.GENERATE_PRE_VOTING_OUTPUTS.value(), order);
		assertFalse(validateParametersAndCommand(commandsForPhase.get(0)));
	}

	/**
	 * Validate parameters of the in action
	 */
	private boolean validateParametersAndCommand(String actionForPhase) {
		boolean result = true;
		if (!actionForPhase.contains("ech-file-converter.jar") || !actionForPhase.contains("copyToUsb.bat") || !actionForPhase
				.contains("copyFromUsb.bat")) {
			result = false;
		} else {
			if (actionForPhase.contains("ech-file-converter.jar")) {
				String[] params = actionForPhase.split(" -");
				result = params.length > 2;
			} else if (actionForPhase.contains("copyToUsb.bat")) {
				String[] params = actionForPhase.split(" ");
				if (params.length != 4 || !((actionForPhase.contains("#USB_LETTER#")) && (actionForPhase.contains("#EE_ID#")) && (actionForPhase
						.contains("#EE_ALIAS#")))) {
					result = false;
				}
			} else if (actionForPhase.contains("copyFromUsb.bat")) {
				if (actionForPhase.length() > 0) {
					result = false;
				}
			}
		}
		return result;
	}
}
