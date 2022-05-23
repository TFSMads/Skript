/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.lang.structure;

import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;
import java.util.Map;

/**
 * An EntryContainer is a data container for {@link Structure}s used for obtaining values from {@link StructureEntryData}.
 */
public class EntryContainer {

	private final SectionNode source;
	@Nullable
	private final StructureEntryValidator entryValidator;
	@Nullable
	private final Map<String, Node> handledNodes;
	private final List<Node> unhandledNodes;

	EntryContainer(
		SectionNode source, @Nullable StructureEntryValidator entryValidator, @Nullable Map<String, Node> handledNodes, List<Node> unhandledNodes
	) {
		this.source = source;
		this.entryValidator = entryValidator;
		this.handledNodes = handledNodes;
		this.unhandledNodes = unhandledNodes;
	}

	/**
	 * @return The SectionNode containing the entries associated with the StructureEntryValidator (or parsed Structure as a whole)
	 */
	public SectionNode getSource() {
		return source;
	}

	/**
	 * @return Any nodes unhandled by the StructureEntryValidator.
	 * {@link StructureEntryValidator#allowsUnknownEntries()} or {@link StructureEntryValidator#allowsUnknownSections()} must be true
	 *   for this list to contain any values. The 'unhandled node' would represent any entry provided by the user that the validator
	 *   is not explicitly aware of.
	 */
	public List<Node> getUnhandledNodes() {
		return unhandledNodes;
	}

	/**
	 * A method for obtaining a non-null, typed entry value.
	 * @param key The key associated with the entry.
	 * @param expectedType The class representing the expected type of the entry's value.
	 * @return The entry's value.
	 * @throws RuntimeException If the entry's value is null, or if it is not of the expected type.
	 */
	public <T> T get(String key, Class<T> expectedType) {
		T parsed = getOptional(key, expectedType);
		if (parsed == null)
			throw new RuntimeException("Null value for asserted non-null value");
		return parsed;
	}

	/**
	 * A method for obtaining a non-null entry value with an unknown type.
	 * @param key The key associated with the entry.
	 * @return The entry's value.
	 * @throws RuntimeException If the entry's value is null.
	 */
	public Object get(String key) {
		Object parsed = getOptional(key);
		if (parsed == null)
			throw new RuntimeException("Null value for asserted non-null value");
		return parsed;
	}

	/**
	 * A method for obtaining a nullable, typed entry value.
	 * @param key The key associated with the entry.
	 * @param expectedType The class representing the expected type of the entry's value.
	 * @return The entry's value.
	 * @throws RuntimeException If the entry's value is not of the expected type.
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public <T> T getOptional(String key, Class<T> expectedType) {
		Object parsed = getOptional(key);
		if (parsed == null)
			return null;
		if (!expectedType.isInstance(parsed))
			throw new RuntimeException("Expected entry with key '" + key + "' to be '" + expectedType + "', but got '" + parsed.getClass() + "'");
		return (T) parsed;
	}

	/**
	 * A method for obtaining a nullable entry value with an unknown type.
	 * @param key The key associated with the entry.
	 * @return The entry's value.
	 */
	@Nullable
	public Object getOptional(String key) {
		if (entryValidator == null || handledNodes == null)
			return null;

		StructureEntryData<?> entryData = null;
		for (StructureEntryData<?> data : entryValidator.entryData) {
			if (data.getKey().equals(key)) {
				entryData = data;
				break;
			}
		}
		if (entryData == null)
			return null;

		Node node = handledNodes.get(key);
		if (node == null)
			return entryData.getDefaultValue();

		return entryData.getValue(node);
	}

}