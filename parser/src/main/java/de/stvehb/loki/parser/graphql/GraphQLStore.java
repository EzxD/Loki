package de.stvehb.loki.parser.graphql;

import de.stvehb.loki.core.ast.source.Field;
import de.stvehb.loki.core.ast.source.Model;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class GraphQLStore {

	private final Map<Model, List<Field>> primaryKeys = new HashMap<>();
	private final List<Field> immutableFields = new ArrayList<>();

}
