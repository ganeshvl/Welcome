package com.entradahealth.entrada.core.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;

/**
 * Helper methods to make working with Jackson more tololerable.
 *
 * No, Jackson, not you. The other one.
 *
 * @author edr
 * @since 29 Aug 2012
 */
public class JsonUtils
{
    // by now I am getting rather tired of static singleton classes

    private JsonUtils() { }

    public static final ObjectMapper mapper;

    static
    {
        mapper = new ObjectMapper();
    }


    public static ObjectNode fromParams(Object... params)
    {
        Preconditions.checkArgument(params.length % 2 == 0, "params must be multiple of 2.");

        ObjectNode obj = mapper.createObjectNode();

        for (int i = 0; i < params.length; i += 2)
        {
            obj.put(params[i].toString(), params[i + 1].toString());
        }

        return obj;
    }
}
