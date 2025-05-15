package com.intellij.lang.jsgraphql.schema.library.impl

import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryEntity
import com.intellij.platform.workspace.storage.ConnectionId
import com.intellij.platform.workspace.storage.EntitySource
import com.intellij.platform.workspace.storage.EntityType
import com.intellij.platform.workspace.storage.GeneratedCodeApiVersion
import com.intellij.platform.workspace.storage.GeneratedCodeImplVersion
import com.intellij.platform.workspace.storage.MutableEntityStorage
import com.intellij.platform.workspace.storage.WorkspaceEntity
import com.intellij.platform.workspace.storage.WorkspaceEntityInternalApi
import com.intellij.platform.workspace.storage.impl.ModifiableWorkspaceEntityBase
import com.intellij.platform.workspace.storage.impl.WorkspaceEntityBase
import com.intellij.platform.workspace.storage.impl.WorkspaceEntityData
import com.intellij.platform.workspace.storage.impl.containers.MutableWorkspaceSet
import com.intellij.platform.workspace.storage.impl.containers.toMutableWorkspaceSet
import com.intellij.platform.workspace.storage.instrumentation.EntityStorageInstrumentation
import com.intellij.platform.workspace.storage.instrumentation.EntityStorageInstrumentationApi
import com.intellij.platform.workspace.storage.metadata.model.EntityMetadata
import com.intellij.platform.workspace.storage.url.VirtualFileUrl
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls

@GeneratedCodeApiVersion(3)
@GeneratedCodeImplVersion(6)
@OptIn(WorkspaceEntityInternalApi::class)
internal class GraphQLLibraryEntityImpl(private val dataSource: GraphQLLibraryEntityData) : GraphQLLibraryEntity, WorkspaceEntityBase(
  dataSource) {

  private companion object {


    private val connections = listOf<ConnectionId>(
    )

  }

  override val identifier: String
    get() {
      readField("identifier")
      return dataSource.identifier
    }

  override val displayName: String
    get() {
      readField("displayName")
      return dataSource.displayName
    }

  override val description: String?
    get() {
      readField("description")
      return dataSource.description
    }

  override val roots: Set<VirtualFileUrl>
    get() {
      readField("roots")
      return dataSource.roots
    }

  override val entitySource: EntitySource
    get() {
      readField("entitySource")
      return dataSource.entitySource
    }

  override fun connectionIdList(): List<ConnectionId> {
    return connections
  }


  internal class Builder(result: GraphQLLibraryEntityData?) : ModifiableWorkspaceEntityBase<GraphQLLibraryEntity, GraphQLLibraryEntityData>(
    result), GraphQLLibraryEntity.Builder {
    internal constructor() : this(GraphQLLibraryEntityData())

    override fun applyToBuilder(builder: MutableEntityStorage) {
      if (this.diff != null) {
        if (existsInBuilder(builder)) {
          this.diff = builder
          return
        }
        else {
          error("Entity GraphQLLibraryEntity is already created in a different builder")
        }
      }

      this.diff = builder
      addToBuilder()
      this.id = getEntityData().createEntityId()
      // After adding entity data to the builder, we need to unbind it and move the control over entity data to builder
      // Builder may switch to snapshot at any moment and lock entity data to modification
      this.currentEntityData = null

      index(this, "roots", this.roots)
      // Process linked entities that are connected without a builder
      processLinkedEntities(builder)
      checkInitialization() // TODO uncomment and check failed tests
    }

    private fun checkInitialization() {
      val _diff = diff
      if (!getEntityData().isEntitySourceInitialized()) {
        error("Field WorkspaceEntity#entitySource should be initialized")
      }
      if (!getEntityData().isIdentifierInitialized()) {
        error("Field GraphQLLibraryEntity#identifier should be initialized")
      }
      if (!getEntityData().isDisplayNameInitialized()) {
        error("Field GraphQLLibraryEntity#displayName should be initialized")
      }
      if (!getEntityData().isRootsInitialized()) {
        error("Field GraphQLLibraryEntity#roots should be initialized")
      }
    }

    override fun connectionIdList(): List<ConnectionId> {
      return connections
    }

    override fun afterModification() {
      val collection_roots = getEntityData().roots
      if (collection_roots is MutableWorkspaceSet<*>) {
        collection_roots.cleanModificationUpdateAction()
      }
    }

    // Relabeling code, move information from dataSource to this builder
    override fun relabel(dataSource: WorkspaceEntity, parents: Set<WorkspaceEntity>?) {
      dataSource as GraphQLLibraryEntity
      if (this.entitySource != dataSource.entitySource) this.entitySource = dataSource.entitySource
      if (this.identifier != dataSource.identifier) this.identifier = dataSource.identifier
      if (this.displayName != dataSource.displayName) this.displayName = dataSource.displayName
      if (this.description != dataSource?.description) this.description = dataSource.description
      if (this.roots != dataSource.roots) this.roots = dataSource.roots.toMutableSet()
      updateChildToParentReferences(parents)
    }


    override var entitySource: EntitySource
      get() = getEntityData().entitySource
      set(value) {
        checkModificationAllowed()
        getEntityData(true).entitySource = value
        changedProperty.add("entitySource")

      }

    override var identifier: String
      get() = getEntityData().identifier
      set(value) {
        checkModificationAllowed()
        getEntityData(true).identifier = value
        changedProperty.add("identifier")
      }

    override var displayName: String
      get() = getEntityData().displayName
      set(value) {
        checkModificationAllowed()
        getEntityData(true).displayName = value
        changedProperty.add("displayName")
      }

    override var description: String?
      get() = getEntityData().description
      set(value) {
        checkModificationAllowed()
        getEntityData(true).description = value
        changedProperty.add("description")
      }

    private val rootsUpdater: (value: Set<VirtualFileUrl>) -> Unit = { value ->
      val _diff = diff
      if (_diff != null) index(this, "roots", value)
      changedProperty.add("roots")
    }
    override var roots: MutableSet<VirtualFileUrl>
      get() {
        val collection_roots = getEntityData().roots
        if (collection_roots !is MutableWorkspaceSet) return collection_roots
        if (diff == null || modifiable.get()) {
          collection_roots.setModificationUpdateAction(rootsUpdater)
        }
        else {
          collection_roots.cleanModificationUpdateAction()
        }
        return collection_roots
      }
      set(value) {
        checkModificationAllowed()
        getEntityData(true).roots = value
        rootsUpdater.invoke(value)
      }

    override fun getEntityClass(): Class<GraphQLLibraryEntity> = GraphQLLibraryEntity::class.java
  }
}

@OptIn(WorkspaceEntityInternalApi::class)
internal class GraphQLLibraryEntityData : WorkspaceEntityData<GraphQLLibraryEntity>() {
  lateinit var identifier: String
  lateinit var displayName: String
  var description: String? = null
  lateinit var roots: MutableSet<VirtualFileUrl>

  internal fun isIdentifierInitialized(): Boolean = ::identifier.isInitialized
  internal fun isDisplayNameInitialized(): Boolean = ::displayName.isInitialized
  internal fun isRootsInitialized(): Boolean = ::roots.isInitialized

  override fun wrapAsModifiable(diff: MutableEntityStorage): WorkspaceEntity.Builder<GraphQLLibraryEntity> {
    val modifiable = GraphQLLibraryEntityImpl.Builder(null)
    modifiable.diff = diff
    modifiable.id = createEntityId()
    return modifiable
  }

  @OptIn(EntityStorageInstrumentationApi::class)
  override fun createEntity(snapshot: EntityStorageInstrumentation): GraphQLLibraryEntity {
    val entityId = createEntityId()
    return snapshot.initializeEntity(entityId) {
      val entity = GraphQLLibraryEntityImpl(this)
      entity.snapshot = snapshot
      entity.id = entityId
      entity
    }
  }

  override fun getMetadata(): EntityMetadata {
    return MetadataStorageImpl.getMetadataByTypeFqn("com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryEntity") as EntityMetadata
  }

  override fun clone(): GraphQLLibraryEntityData {
    val clonedEntity = super.clone()
    clonedEntity as GraphQLLibraryEntityData
    clonedEntity.roots = clonedEntity.roots.toMutableWorkspaceSet()
    return clonedEntity
  }

  override fun getEntityInterface(): Class<out WorkspaceEntity> {
    return GraphQLLibraryEntity::class.java
  }

  override fun createDetachedEntity(parents: List<WorkspaceEntity.Builder<*>>): WorkspaceEntity.Builder<*> {
    return GraphQLLibraryEntity(identifier, displayName, roots, entitySource) {
      this.description = this@GraphQLLibraryEntityData.description
    }
  }

  override fun getRequiredParents(): List<Class<out WorkspaceEntity>> {
    val res = mutableListOf<Class<out WorkspaceEntity>>()
    return res
  }

  override fun equals(other: Any?): Boolean {
    if (other == null) return false
    if (this.javaClass != other.javaClass) return false

    other as GraphQLLibraryEntityData

    if (this.entitySource != other.entitySource) return false
    if (this.identifier != other.identifier) return false
    if (this.displayName != other.displayName) return false
    if (this.description != other.description) return false
    if (this.roots != other.roots) return false
    return true
  }

  override fun equalsIgnoringEntitySource(other: Any?): Boolean {
    if (other == null) return false
    if (this.javaClass != other.javaClass) return false

    other as GraphQLLibraryEntityData

    if (this.identifier != other.identifier) return false
    if (this.displayName != other.displayName) return false
    if (this.description != other.description) return false
    if (this.roots != other.roots) return false
    return true
  }

  override fun hashCode(): Int {
    var result = entitySource.hashCode()
    result = 31 * result + identifier.hashCode()
    result = 31 * result + displayName.hashCode()
    result = 31 * result + description.hashCode()
    result = 31 * result + roots.hashCode()
    return result
  }

  override fun hashCodeIgnoringEntitySource(): Int {
    var result = javaClass.hashCode()
    result = 31 * result + identifier.hashCode()
    result = 31 * result + displayName.hashCode()
    result = 31 * result + description.hashCode()
    result = 31 * result + roots.hashCode()
    return result
  }
}
