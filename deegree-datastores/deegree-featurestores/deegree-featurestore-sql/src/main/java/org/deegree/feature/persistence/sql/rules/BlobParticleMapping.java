package org.deegree.feature.persistence.sql.rules;

import java.util.List;

import org.apache.xerces.xs.XSElementDeclaration;
import org.deegree.feature.persistence.sql.expressions.TableJoin;
import org.deegree.feature.persistence.sql.jaxb.CustomConverterJAXB;
import org.deegree.filter.expression.ValueReference;
import org.deegree.sqldialect.filter.MappingExpression;

/**
 * BLOB {@link Mapping} of complex particles.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.4
 */
public class BlobParticleMapping extends Mapping {

    private final MappingExpression mapping;

    private final XSElementDeclaration elDecl;

    /**
     * Creates a new {@link BlobParticleMapping} instance.
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

    /**
     * Returns the mapping expression.
     * 
     * @return mapping expression, never <code>null</code>
     */
    public MappingExpression getMapping() {
        return mapping;
    }

    /**
     * Returns the element declaration.
     * 
     * @return element declaration, never <code>null</code>
     */
    public XSElementDeclaration getElementDeclaration() {
        return elDecl;
    }

}
