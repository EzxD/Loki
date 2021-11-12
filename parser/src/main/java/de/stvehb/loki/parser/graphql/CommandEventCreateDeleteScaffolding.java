package de.stvehb.loki.parser.graphql;

import de.stvehb.loki.core.ast.Project;
import de.stvehb.loki.core.ast.source.Annotation;
import de.stvehb.loki.core.ast.source.Enum;
import de.stvehb.loki.core.ast.source.Field;
import de.stvehb.loki.core.ast.source.Model;
import de.stvehb.loki.core.option.Context;
import de.stvehb.loki.core.util.Naming;

import java.util.ArrayList;
import java.util.List;

/**
 * This scaffolding is responsible for creating commands and events for creating and deleting entities/aggregates.
 */
public class CommandEventCreateDeleteScaffolding {

	private static final Annotation AGGREGATE_DELETED_EVENT = new Annotation("AggregateDeletedEvent", "dev.askrella.eventsourcing.model");

	public static List<Model> process(Context context, Project project) {
		GraphQLStore store = context.retrieve(GraphQLStore.class);
		List<Model> commandAndEvents = new ArrayList<>();
		project.getModels().forEach(model -> {
			if (model instanceof Enum) return;

			List<Field> primaryKeys = store.getPrimaryKeys().get(model);
			commandAndEvents.add(createCommand(model.getName(), primaryKeys, "Create"));
			commandAndEvents.add(createCommand(model.getName(), primaryKeys, "Delete"));
			commandAndEvents.add(createEvent(model.getName(), primaryKeys, "Created"));
			commandAndEvents.add(createEvent(model.getName(), primaryKeys, "Deleted"));
		});

		return commandAndEvents;
	}

	private static Model createCommand(String modelName, List<Field> primaryKeys, String operation) {
		Model command = new Model();
		command.setName(operation + modelName + "Command");

		command.setFields(new ArrayList<>(primaryKeys));

		System.out.println("Generate command: " + command.getName());
		for (Field primaryKeyField : primaryKeys) {
			System.out.println("	" + primaryKeyField.getName());
		}

		return command;
	}

	private static Model createEvent(String modelName, List<Field> primaryKeys, String operation) {
		Model event = new Model();
		event.setName(modelName + operation + "Event");

		event.setFields(new ArrayList<>(primaryKeys));

		if (operation.equals("Deleted")) event.getAnnotations().add(AGGREGATE_DELETED_EVENT);

		System.out.println("Generate event: " + event.getName());
		for (Field primaryKeyField : primaryKeys) {
			System.out.println("	" + primaryKeyField.getName());
		}

		return event;
	}

}
