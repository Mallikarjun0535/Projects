package com.dizzion.portal.domain.common.persistence;

import org.springframework.stereotype.Component;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.Iterator;

import static java.util.Arrays.copyOfRange;
import static java.util.Arrays.stream;

@Component
public class CriteriaPathFactory {

    public <R, P> Path<P> criteriaPathFromString(String criteriaPathString, Root<R> criteriaRoot) {
        String[] pathComponents = criteriaPathString.split("\\.");
        if (stream(pathComponents).anyMatch(String::isEmpty)) {
            throw new IllegalArgumentException("Invalid criteria path");
        }

        String firstComponent = pathComponents[0];
        String[] additionalComponents = copyOfRange(pathComponents, 1, pathComponents.length);

        Path<P> path = criteriaRoot.get(firstComponent);
        if (isCollection(path) && additionalComponents.length > 0) {
            Join<P, JoinType> intermediateJoin = criteriaRoot.join(firstComponent);
            Iterator<String> componentsIterator = stream(additionalComponents).iterator();

            while (componentsIterator.hasNext()) {
                String component = componentsIterator.next();
                path = intermediateJoin.get(component);
                if (isCollection(path) && componentsIterator.hasNext()) {
                    intermediateJoin = intermediateJoin.join(component);
                }
            }
        } else {
            for (String component : additionalComponents) {
                path = path.get(component);
            }
        }
        return path;
    }

    private boolean isCollection(Path path) {
        return Collection.class.isAssignableFrom(path.getJavaType());
    }
}
