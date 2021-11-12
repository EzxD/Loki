package de.stvehb.loki.parser;

import com.google.common.io.Resources;
import com.google.gson.GsonBuilder;
import de.stvehb.loki.core.ast.Author;
import de.stvehb.loki.core.ast.Project;
import de.stvehb.loki.core.ast.ProjectInfo;
import de.stvehb.loki.core.ast.source.Annotation;
import de.stvehb.loki.core.ast.source.BuiltInType;
import de.stvehb.loki.core.ast.source.Field;
import de.stvehb.loki.core.ast.source.Model;
import de.stvehb.loki.core.option.Context;
import de.stvehb.loki.core.util.Naming;
import de.stvehb.loki.parser.graphql.*;
import graphql.language.*;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.Getter;
import lombok.SneakyThrows;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GraphQLParser {

	public static final Annotation ANNOTATION_NOT_BLANK = new Annotation("NotBlank", "javax.validation.constraints");
	public static final Annotation ANNOTATION_NOT_NULL = new Annotation("NotNull", "javax.validation.constraints");
	@Getter private static final Map<String, de.stvehb.loki.core.ast.source.Type> NAME_TO_TYPE = new HashMap<>();

	static {
		NAME_TO_TYPE.put("String", new BuiltInType("String"));
		NAME_TO_TYPE.put("Int", new BuiltInType("int"));
		NAME_TO_TYPE.put("Boolean", new BuiltInType("boolean"));

		de.stvehb.loki.core.ast.source.Type idType = new de.stvehb.loki.core.ast.source.Type();
		idType.setNamespace("java.util");
		idType.setName("UUID");
		NAME_TO_TYPE.put("ID", idType);
	}

	@SuppressWarnings({"UnstableApiUsage", "rawtypes"})
	@SneakyThrows
	public static Project parse(Context context, String schema) {
		context.store(new GraphQLStore());

		Project project = new Project();
		project.setModels(new ArrayList<>());
		project.setEnums(new ArrayList<>());
		project.setDependencies(new ArrayList<>());
		project.setCompilerOptions(new ArrayList<>());

		project.setInfo(new ProjectInfo("Test", "1.0", "dev.askrella.product.model", new Author("Askrella", "steve@askrella.de", new String[]{"Maintainer", "Developer"})));

		URL url = Resources.getResource("product.graphql");
		schema = Resources.toString(url, StandardCharsets.UTF_8);

		SchemaParser schemaParser = new SchemaParser();
		TypeDefinitionRegistry registry = schemaParser.parse(schema);

		// Register default types/scalars
		NAME_TO_TYPE.forEach((name, type) -> context.getTypes().add(type));

		TypeResolver.parse(context, project, registry);
		ModelParser.parse(context, project, registry);

		List<Model> commandAndEvents = new ArrayList<>();
		commandAndEvents.addAll(CommandEventUpdateScaffolding.process(context, project));
		commandAndEvents.addAll(CommandEventCreateDeleteScaffolding.process(context, project));
		project.getModels().addAll(commandAndEvents);

		System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(project));
		return project;
	}

	public static boolean isArray(Type type) {
		return !isNonNull(type) ? type instanceof ListType : isArray(((NonNullType) type).getType());
	}

	public static boolean isNonNull(Type type) {
		return type instanceof NonNullType;
	}

	public static de.stvehb.loki.core.ast.source.Type getType(Context context, Project project, Type type) {
		if (type instanceof ListType) return getType(context, project, ((ListType) type).getType());
		if (type instanceof NonNullType) return getType(context, project, ((NonNullType) type).getType());

		TypeName typeName = (TypeName) type;
		de.stvehb.loki.core.ast.source.Type lokiType = context.getTypes().stream().filter(t -> t.getName().equals(typeName.getName())).findFirst().orElse(null);
		if (lokiType == null) lokiType = NAME_TO_TYPE.get(typeName.getName());

		Optional<Model> definedType = project.getModels().stream().filter(model -> model.getName().equalsIgnoreCase(typeName.getName())).findFirst();
		if (definedType.isPresent()) return definedType.get();

		if (lokiType == null) throw new RuntimeException("Unknown graphql type: " + typeName.getName());
		return lokiType;
	}

}
