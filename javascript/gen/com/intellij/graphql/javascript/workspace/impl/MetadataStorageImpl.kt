package com.intellij.graphql.javascript.workspace.impl

import com.intellij.platform.workspace.storage.WorkspaceEntityInternalApi
import com.intellij.platform.workspace.storage.metadata.impl.MetadataStorageBase
import com.intellij.platform.workspace.storage.metadata.model.EntityMetadata
import com.intellij.platform.workspace.storage.metadata.model.FinalClassMetadata
import com.intellij.platform.workspace.storage.metadata.model.OwnPropertyMetadata
import com.intellij.platform.workspace.storage.metadata.model.StorageTypeMetadata
import com.intellij.platform.workspace.storage.metadata.model.ValueTypeMetadata

@OptIn(WorkspaceEntityInternalApi::class)
internal object MetadataStorageImpl : MetadataStorageBase() {
  override fun initializeMetadata() {
    val primitiveTypeSetNotNullable = ValueTypeMetadata.SimpleType.PrimitiveType(isNullable = false, type = "Set")

    var typeMetadata: StorageTypeMetadata

    typeMetadata = FinalClassMetadata.ObjectMetadata(fqName = "com.intellij.graphql.javascript.workspace.GraphQLNodeModulesEntitySource",
                                                     properties = listOf(
                                                       OwnPropertyMetadata(isComputable = false, isKey = false, isOpen = false,
                                                                           name = "virtualFileUrl",
                                                                           valueType = ValueTypeMetadata.SimpleType.CustomType(
                                                                             isNullable = true,
                                                                             typeMetadata = FinalClassMetadata.KnownClass(
                                                                               fqName = "com.intellij.platform.workspace.storage.url.VirtualFileUrl")),
                                                                           withDefault = false)),
                                                     supertypes = listOf("com.intellij.platform.workspace.storage.EntitySource"))

    addMetadata(typeMetadata)

    typeMetadata = EntityMetadata(fqName = "com.intellij.graphql.javascript.workspace.GraphQLNodeModulesEntity",
                                  entityDataFqName = "com.intellij.graphql.javascript.workspace.impl.GraphQLNodeModulesEntityData",
                                  supertypes = listOf("com.intellij.platform.workspace.storage.WorkspaceEntity"), properties = listOf(
        OwnPropertyMetadata(isComputable = false, isKey = false, isOpen = false, name = "entitySource",
                            valueType = ValueTypeMetadata.SimpleType.CustomType(isNullable = false,
                                                                                typeMetadata = FinalClassMetadata.KnownClass(
                                                                                  fqName = "com.intellij.platform.workspace.storage.EntitySource")),
                            withDefault = false),
        OwnPropertyMetadata(isComputable = false, isKey = false, isOpen = false, name = "roots",
                            valueType = ValueTypeMetadata.ParameterizedType(generics = listOf(
                              ValueTypeMetadata.SimpleType.CustomType(isNullable = false, typeMetadata = FinalClassMetadata.KnownClass(
                                fqName = "com.intellij.platform.workspace.storage.url.VirtualFileUrl"))),
                                                                            primitive = primitiveTypeSetNotNullable), withDefault = false)),
                                  extProperties = listOf(), isAbstract = false)

    addMetadata(typeMetadata)
  }

  override fun initializeMetadataHash() {
    addMetadataHash(typeFqn = "com.intellij.graphql.javascript.workspace.GraphQLNodeModulesEntity", metadataHash = -306196251)
    addMetadataHash(typeFqn = "com.intellij.platform.workspace.storage.EntitySource", metadataHash = -2106051587)
    addMetadataHash(typeFqn = "com.intellij.graphql.javascript.workspace.GraphQLNodeModulesEntitySource", metadataHash = -645610419)
  }

}
