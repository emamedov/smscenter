package org.eminmamedov.smscenter.datamodel;

import java.io.Serializable;

/**
 * Base class for all domain entities
 * 
 * @author Emin Mamedov
 * 
 */
public abstract class Entity implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * Getter for primary key of entity
     * 
     * @return entity's id
     */
    public Long getId() {
        return id;
    }

    /**
     * Setter for primary key of entity
     * 
     * @param id
     *            entity's new id
     */
    public void setId(Long id) {
        this.id = id;
    }

}
