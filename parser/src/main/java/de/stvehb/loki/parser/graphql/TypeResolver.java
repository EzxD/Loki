package de.stvehb.loki.parser.graphql;

import de.stvehb.loki.core.ast.Project;
import de.stvehb.loki.core.ast.source.Enum;
import de.stvehb.loki.core.ast.source.Field;
import de.stvehb.loki.core.ast.source.Model;
import de.stvehb.loki.core.option.Context;
import graphql.language.EnumTypeDefinition;
import graphql.language.InputObjectTypeDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This resolver will parse all custom type definitions used later on to identify the type of fields.
 */
public class TypeResolver {

	public static void parse(Context context, Project project, TypeDefinitionRegistry registry) {
		context.getTypes().addAll(registry.types().values().stream().map(typeDefinition -> {
			if (typeDefinition instanceof ObjectTypeDefinition) {
				Model model = parseModel((ObjectTypeDefinition) typeDefinition);
				project.getModels().add(model);
				return model;
			}
			if (typeDefinition instanceof EnumTypeDefinition) {
				Enum enumModel = parseEnum((EnumTypeDefinition) typeDefinition);
				project.getEnums().add(enumModel);
				return enumModel;
			}
			if (typeDefinition instanceof InputObjectTypeDefinition) {
				Model model = parseInput((InputObjectTypeDefinition) typeDefinition);
				project.getModels().add(model);
				return model;
			}

			throw new RuntimeException("Cannot handle: " + typeDefinition);
		}).collect(Collectors.toList()));
	}

	private static Model parseModel(ObjectTypeDefinition definition) {
		Model model = new Model();
		model.setName(definition.getName());
		model.setFields(new ArrayList<>());
		model.setImports(new ArrayList<>());
		model.setDocumentation(definition.getDescription() != null ? definition.getDescription().getContent() : null);
		return model;
	}

	private static Enum parseEnum(EnumTypeDefinition definition) {
		Enum enumModel = new Enum();
		enumModel.setName(definition.getName());
		enumModel.setDocumentation(definition.getDescription() != null ? definition.getDescription().getContent() : null);
		enumModel.setFields(definition.getEnumValueDefinitions().stream().map(enumValueDefinition ->
			new Field(List.of(), null, false, null, enumValueDefinition.getName(),
				enumValueDefinition.getDescription() != null ? enumValueDefinition.getDescription().getContent() : null)
		).collect(Collectors.toList()));

		return enumModel;
	}

	private static Model parseInput(InputObjectTypeDefinition definition) {
		return null;//TODO
	}

}
