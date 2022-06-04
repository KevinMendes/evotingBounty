/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.integration.plugin;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PluginSequenceResolver {

	private final Plugins plugins;

	public PluginSequenceResolver(final Plugins plugins) {
		this.plugins = plugins;
	}

	public List<String> getActionsForPhase(final String name) {
		final PhaseName phaseName = PhaseName.fromValue(name);
		return getActionsForPhase(phaseName);
	}

	public List<String> getActionsForPhase(final PhaseName phaseName) {

		final Optional<Phase> selectedPhase = findPhaseBasedOnProperties(phaseName);

		if (selectedPhase.isPresent()) {
			return retrieveCommandLineActionsInOrder(selectedPhase.get());
		}
		return Collections.emptyList();
	}

	private Optional<Phase> findPhaseBasedOnProperties(final PhaseName phaseName) {
		return plugins.getPhase().stream().filter(phase -> phaseName.equals(phase.getName())).findAny();
	}

	private List<String> retrieveCommandLineActionsInOrder(final Phase phase) {
		final List<Plugin> pluginList = phase.getPlugin();
		final List<String> actionList = pluginList.stream().sorted((p1, p2) -> p1.getOrder().compareTo(p2.getOrder())).map(sc -> sc.getValue())
				.collect(Collectors.toList());
		return actionList;
	}

	private List<String> retrieveCommandLineActionsByOrder(final Phase phase, final Integer order) {
		final List<Plugin> pluginList = phase.getPlugin();
		final List<String> actionList = pluginList.stream().filter(p -> p.getOrder().equals(order)).map(sc -> sc.getValue()).collect(Collectors.toList());
		return actionList;
	}

	public List<String> getActionsForPhaseAndOrder(final String name, final Integer order) {
		final PhaseName phaseName = PhaseName.fromValue(name);

		final Optional<Phase> selectedPhase = findPhaseBasedOnProperties(phaseName);

		if (selectedPhase.isPresent()) {
			return retrieveCommandLineActionsByOrder(selectedPhase.get(), order);
		}
		return Collections.emptyList();
	}

}
