package de.stvehb.loki.parser.graphql;

import de.stvehb.loki.core.ast.Project;
import de.stvehb.loki.core.ast.source.Field;
import de.stvehb.loki.core.ast.source.Model;
import de.stvehb.loki.core.option.Context;
import graphql.language.InputObjectTypeDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static de.stvehb.loki.parser.GraphQLParser.*;

/**
 * This parser will add the fields to their corresponding models (type/input in graphql).
 */
public class ModelParser {

	public static void parse(Context context, Project project, TypeDefinitionRegistry registry) {
		registry.types().values().forEach(typeDefinition -> {
			Model model = (Model) context.getTypes().stream().filter(type -> type.getName().equalsIgnoreCase(typeDefinition.getName())).findFirst().get();
			if (typeDefinition instanceof ObjectTypeDefinition) parseModel(context, project, model, (ObjectTypeDefinition) typeDefinition);
			if (typeDefinition instanceof InputObjectTypeDefinition) parseInput(context, project, model, (InputObjectTypeDefinition) typeDefinition);
		});
	}

	private static void parseModel(Context context, Project project, Model model, ObjectTypeDefinition objectTypeDefinition) {
		objectTypeDefinition.getFieldDefinitions().forEach(fieldDefinition -> {
			Field field = new Field(
				isNonNull(fieldDefinition.getType()) ? List.of(ANNOTATION_NOT_NULL) : List.of(), // Annotations
				getType(context, project, fieldDefinition.getType()),
				isArray(fieldDefinition.getType()),
				null,
				fieldDefinition.getName(),
				fieldDefinition.getDescription() != null ? fieldDefinition.getDescription().getContent() : null
			);

			List<Field> primaryKeys = context.retrieve(GraphQLStore.class).getPrimaryKeys().computeIfAbsent(model, ignore -> new ArrayList<>());
			if (Objects.toString(field.getDocumentation(), "").contains("@id")) {
				field.setDocumentation(field.getDocumentation().replace("@id", ""));
				primaryKeys.add(field);
			}

			if (Objects.toString(field.getDocumentation(), "").contains("@immutable")) {
				field.setDocumentation(field.getDocumentation().replace("@immutable", ""));
				context.retrieve(GraphQLStore.class).getImmutableFields().add(field);
			}

			if (Objects.toString(field.getDocumentation(), "").isBlank()) field.setDocumentation(null);

			model.getFields().add(field);
		});
	}

	private static void parseInput(Context context, Project project, Model model, InputObjectTypeDefinition inputDefinition) {
		//TODO
	}

}
