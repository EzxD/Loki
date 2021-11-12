package de.stvehb.loki.core.option;

import de.stvehb.loki.core.ast.Project;
import de.stvehb.loki.core.ast.source.Type;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class Context {

	private Project project;
	private final List<Type> types = new ArrayList<>();
	private DebugOptions debugOptions;

	private final DebuggingStore debuggingStore = new DebuggingStore();
	private final Map<Class<?>, Object> extraData = new HashMap<>();

	public void store(Object data) {
		this.extraData.put(data.getClass(), data);
	}

	@SuppressWarnings("unchecked")
	public <T> T retrieve(Class<T> clazz) {
		return (T) this.extraData.get(clazz);
	}

}
