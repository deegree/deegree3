package org.deegree.feature.persistence.sql.rules;

import java.util.List;

import org.apache.xerces.xs.XSElementDeclaration;
import org.deegree.feature.persistence.sql.expressions.TableJoin;
import org.deegree.feature.persistence.sql.jaxb.CustomConverterJAXB;
import org.deegree.filter.expression.ValueReference;
import org.deegree.sqldialect.filter.MappingExpression;

public class BlobParticleMapping extends Mapping {

    private final MappingExpression mapping;

    private final XSElementDeclaration elDecl;

    /**
     *
     * @param path
     *            relative xpath expression, must not be <code>null</code>
     * @param voidable
     *            <code>true</code>, if the particle can be omitted from the parent particle (i.e. be <code>null</code>
     *            ), <code>false</code> otherwise
     * @param mapping
     *            mapping expression (DB column), must not be <code>null</code>
     * @param elDecl
     *            XML schema declaration of the mapped element, must not be <code>null</code>
     * @param tableChange
     *            table joins, can be <code>null</code> (no joins involved)
     */
    public BlobParticleMapping( ValueReference path, boolean voidable, MappingExpression mapping,
                                XSElementDeclaration elDecl, List<TableJoin> tableChange, CustomConverterJAXB converter ) {
        super( path, voidable, tableChange, converter );
        this.mapping = mapping;
        this.elDecl = elDecl;
    }

    public MappingExpression getMapping() {
        return mapping;
    }

    public XSElementDeclaration getElementDeclaration() {
        return elDecl;
    }
}
