/*
 * Copyright (c) 2012, Francis Galiegue <fgaliegue@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eel.kitchen.jsonschema.main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.eel.kitchen.jsonschema.util.JacksonUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public final class ValidationMessage
{
    private static final JsonNodeFactory factory = JsonNodeFactory.instance;
    private static final Joiner JOINER = Joiner.on("; ");

    private final ValidationDomain domain;
    private final String keyword;
    private final String message;
    private final Map<String, JsonNode> info;

    private ValidationMessage(final Builder builder)
    {
        domain = builder.domain;
        keyword = builder.keyword;
        message = builder.message;
        info = ImmutableMap.copyOf(JacksonUtils.nodeToMap(builder.info));
    }

    public static Builder defaultBuilder()
    {
        return new Builder(ValidationDomain.UNKNOWN).setKeyword("(not set)");
    }

    public ValidationDomain getDomain()
    {
        return domain;
    }

    public String getKeyword()
    {
        return keyword;
    }

    public String getMessage()
    {
        return message;
    }

    public JsonNode getInfo(final String key)
    {
        return info.get(key);
    }

    public JsonNode getInfo()
    {
        return factory.objectNode().putAll(info);
    }

    public JsonNode toJsonNode()
    {
        final ObjectNode ret = factory.objectNode()
            .put("domain", domain.toString()).put("keyword", keyword)
            .put("message", message);

        ret.putAll(info);
        return ret;
    }

    @Override
    public int hashCode()
    {
        int ret = domain.hashCode();
        ret *= 31;
        ret += keyword.hashCode();
        ret *= 31;
        ret += message.hashCode();
        ret *= 31;
        ret += info.hashCode();
        return ret;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == null)
            return false;
        if (this == obj)
            return true;

        if (getClass() != obj.getClass())
            return false;

        final ValidationMessage other = (ValidationMessage) obj;

        return domain == other.domain
            && keyword.equals(other.keyword)
            && message.equals(other.message)
            && info.equals(other.info);
    }

    @Override
    public String toString()
    {
        final List<String> list = new ArrayList<String>();

        list.add("domain: " + domain.toString());
        list.add("keyword: " + keyword);
        list.add("message: " + message);

        final SortedSet<String> infoKeys = new TreeSet<String>(info.keySet());

        for (final String key: infoKeys)
            list.add(key + ": " + info.get(key));

        return JOINER.join(list);
    }

    public static final class Builder
    {
        private static final Set<String> RESERVED = ImmutableSet.of("domain",
            "keyword", "message");

        private final ValidationDomain domain;
        private String keyword;
        private String message;
        private final ObjectNode info = factory.objectNode();

        public Builder(final ValidationDomain domain)
        {
            Preconditions.checkNotNull(domain, "domain is null");
            this.domain = domain;
        }

        public Builder setKeyword(final String keyword)
        {
            this.keyword = keyword;
            return this;
        }

        public Builder setMessage(final String message)
        {
            this.message = message;
            return this;
        }

        public Builder addInfo(final String key, final JsonNode value)
        {
            info.put(key, value);
            return this;
        }

        public <T> Builder addInfo(final String key, final T value)
        {
            info.put(key, value.toString());
            return this;
        }

        public <T> Builder addInfo(final String key, final Collection<T> values)
        {
            final ArrayNode node = factory.arrayNode();

            for (final T value: values)
                node.add(value.toString());

            info.put(key, node);
            return this;
        }

        public Builder addInfo(final String key, final int value)
        {
            info.put(key, value);
            return this;
        }

        public Builder clearInfo()
        {
            info.removeAll();
            return this;
        }

        public ValidationMessage build()
        {
            Preconditions.checkNotNull(keyword, "keyword is null");
            Preconditions.checkNotNull(message, "message is null");
            info.remove(RESERVED);

            return new ValidationMessage(this);
        }
    }
}