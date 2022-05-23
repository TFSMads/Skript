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
package ch.njol.skript.structures;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.Script;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.structure.EntryContainer;
import ch.njol.skript.lang.structure.Structure;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.Signature;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

public class StructFunction extends Structure {

	public static final Priority PRIORITY = new Priority(400);

	private static final AtomicBoolean validateFunctions = new AtomicBoolean();

	static {
		Skript.registerStructure(StructFunction.class, "function <.+>");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private SectionNode node;
	@Nullable
	private Signature<?> signature;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult, EntryContainer entryContainer) {
		this.node = entryContainer.getSource();
		return true;
	}

	@Override
	public void preLoad() {
		Script script = getParser().getCurrentScript();
		if (script == null)
			throw new IllegalStateException("Current script is null during function loading");
		signature = Functions.loadSignature(script.getConfig().getFileName(), node);
	}

	@Override
	public void load() {
		getParser().setCurrentEvent("function", FunctionEvent.class);

		Functions.loadFunction(getParser().getCurrentScript(), node);

		getParser().deleteCurrentEvent();

		validateFunctions.set(true);
	}

	@Override
	public void postLoad() {
		if (validateFunctions.get()) {
			validateFunctions.set(false);
			Functions.validateFunctions();
		}
	}

	@Override
	public void unload() {
		if (signature != null)
			Functions.unregisterFunction(signature);
		validateFunctions.set(true);
	}

	@Override
	public Priority getPriority() {
		return PRIORITY;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "function";
	}

}