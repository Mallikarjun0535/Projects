package com.dizzion.portal.infra.swagger;

import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.google.common.base.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelPropertyBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelPropertyContext;

import static springfox.documentation.swagger.common.SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER;

@Component
@Order(SWAGGER_PLUGIN_ORDER)
public class OptionalModelProperties implements ModelPropertyBuilderPlugin {

    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }

    @Override
    public void apply(ModelPropertyContext context) {
        Optional<BeanPropertyDefinition> propertyDefinition = context.getBeanPropertyDefinition();
        if (propertyDefinition.isPresent()) {
            if (!propertyDefinition.get().getGetter().getMember().getReturnType().equals(java.util.Optional.class)) {
                context.getBuilder().required(true);
            }
        }
    }
}
