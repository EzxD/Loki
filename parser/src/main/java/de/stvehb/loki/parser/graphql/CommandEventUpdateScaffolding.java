package de.stvehb.loki.parser.graphql;

import de.stvehb.loki.core.ast.Project;
import de.stvehb.loki.core.ast.source.Field;
import de.stvehb.loki.core.ast.source.Model;
import de.stvehb.loki.core.option.Context;
import de.stvehb.loki.core.util.Naming;

import java.util.ArrayList;
import java.util.List;

/**
 * This scaffolding is responsible for creating commands and corresponding events for
 * updates applied to mutable, non-id fields.
 */
public class CommandEventUpdateScaffolding {

	public static List<Model> process(Context context, Project project) {
		GraphQLStore store = context.retrieve(GraphQLStore.class);
		List<Model> commandAndEvents = new ArrayList<>();
		project.getModels().forEach(model -> {
			model.getFields().stream().filter(field ->
				!store.getPrimaryKeys().get(model).contains(field) &&
				!store.getImmutableFields().contains(field)
			).forEach(mutableField -> {
				commandAndEvents.add(createCommand(
					model.getName(),
					store.getPrimaryKeys().get(model),
					mutableField
				));
				commandAndEvents.add(createEvent(
					model.getName(),
					store.getPrimaryKeys().get(model),
					mutableField
				));
			});
		});

		return commandAndEvents;
	}

	private static Model createCommand(String modelName, List<Field> primaryKeys, Field field) {
		Model command = new Model();
		command.setName("Update" + modelName + Naming.toJavaClass(field.getName()) + "Command");

		List<Field> commandFields = new ArrayList<>();
		commandFields.add(field);
		commandFields.addAll(primaryKeys);
		command.setFields(commandFields);

		System.out.println("Generate command: " + command.getName());
		System.out.println("	" + field.getName());
		for (Field primaryKeyField : primaryKeys) {
			System.out.println("	" + primaryKeyField.getName());
		}

		return command;
	}

	private static Model createEvent(String modelName, List<Field> primaryKeys, Field field) {
		Model event = new Model();
		event.setName(modelName + Naming.toJavaClass(field.getName()) + "UpdatedEvent");

		List<Field> commandFields = new ArrayList<>();
		commandFields.add(field);
		commandFields.addAll(primaryKeys);
		event.setFields(commandFields);

		System.out.println("Generate event: " + event.getName());
		System.out.println("	" + field.getName());
		for (Field primaryKeyField : primaryKeys) {
			System.out.println("	" + primaryKeyField.getName());
		}

		return event;
	}

}
