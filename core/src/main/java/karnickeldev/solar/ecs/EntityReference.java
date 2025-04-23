package karnickeldev.solar.ecs;

import karnickeldev.solar.core.Logger;

public class EntityReference {

    private int entityId;
    private EntityManager entityManager;

    public static EntityReference create(EntityManager entityManager, int entityId) {
        if(entityManager == null || !entityManager.isValid(entityId)) {
            Logger.error("Erroneous EntityReference");
            return null;
        }
        return new EntityReference(entityManager, entityId);
    }

    private EntityReference(EntityManager entityManager, int entityId) {
        this.entityId = entityId;
        this.entityManager = entityManager;
    }

    public int getEntityId() {
        return entityId;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public boolean isValid() {
        return entityManager.isValid(entityId);
    }

}
