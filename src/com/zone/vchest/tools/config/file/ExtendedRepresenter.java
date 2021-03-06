/************************************************************************
 * This file is part of AdminCmd.									
 *																		
 * AdminCmd is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by	
 * the Free Software Foundation, either version 3 of the License, or		
 * (at your option) any later version.									
 *																		
 * AdminCmd is distributed in the hope that it will be useful,	
 * but WITHOUT ANY WARRANTY; without even the implied warranty of		
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the			
 * GNU General Public License for more details.							
 *																		
 * You should have received a copy of the GNU General Public License
 * along with AdminCmd.  If not, see <http://www.gnu.org/licenses/>.
 ************************************************************************/
package com.zone.vchest.tools.config.file;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.CollectionNode;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

/**
 * @author Balor (aka Antoine Aflalo)
 * 
 */
class ExtendedRepresenter extends Representer {

	protected class EmptyRepresentNull implements Represent {
		
		public Node representData(Object data) {
			return representScalar(Tag.NULL, ""); // Changed "null" to "" so as
													// to avoid writing nulls
		}
	}

	private class RepresentConfigurationSection extends RepresentMap {
		
		public Node representData(Object data) {
			return super.representData(((ConfigurationSection) data).getValues(false));
		}
	}

	private class RepresentConfigurationSerializable extends RepresentMap {
		
		public Node representData(Object data) {
			final ConfigurationSerializable serializable = (ConfigurationSerializable) data;
			final Map<String, Object> values = new LinkedHashMap<String, Object>();
			values.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY,
					ConfigurationSerialization.getAlias(serializable.getClass()));
			values.putAll(serializable.serialize());

			return super.representData(values);
		}
	}

	public ExtendedRepresenter() {
		super();
		this.nullRepresenter = new EmptyRepresentNull();
		this.multiRepresenters.put(ConfigurationSection.class, new RepresentConfigurationSection());
		this.multiRepresenters.put(ConfigurationSerializable.class,
				new RepresentConfigurationSerializable());
	}

	// Code borrowed from snakeyaml
	// (http://code.google.com/p/snakeyaml/source/browse/src/test/java/org/yaml/snakeyaml/issues/issue60/SkipBeanTest.java)
	
	protected NodeTuple representJavaBeanProperty(Object javaBean, Property property,
			Object propertyValue, Tag customTag) {
		final NodeTuple tuple = super.representJavaBeanProperty(javaBean, property, propertyValue,
				customTag);
		final Node valueNode = tuple.getValueNode();
		if (valueNode instanceof CollectionNode) {
			// Removed null check
			if (Tag.SEQ.equals(valueNode.getTag())) {
				final SequenceNode seq = (SequenceNode) valueNode;
				if (seq.getValue().isEmpty()) {
					return null; // skip empty lists
				}
			}
			if (Tag.MAP.equals(valueNode.getTag())) {
				final MappingNode seq = (MappingNode) valueNode;
				if (seq.getValue().isEmpty()) {
					return null; // skip empty maps
				}
			}
		}
		return tuple;
	}

}
