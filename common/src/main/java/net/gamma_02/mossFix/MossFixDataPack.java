package net.gamma_02.mossFix;

import com.google.common.collect.ImmutableSet;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
#if MC_NVERSION > 12004
import net.minecraft.server.packs.repository.KnownPack;
import java.util.Optional;
#elif MC_NVERSION >= 12002
import java.util.Optional;
#endif
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Set;
import java.util.stream.Stream;

//note: a lot of this infrastructure is heavily inspired by SimpleVoiceChat! Thank you henkelmax for making your code public!!!
public class MossFixDataPack extends AbstractPackResources implements Pack.ResourcesSupplier {

    private final String[] extraNamespaces;

    private final PackPlatform platform;

    #if MC_NVERSION > 12004

    public MossFixDataPack(String id, Component name, Set<String> extraNamespaces, PackPlatform platform) {
        super(new PackLocationInfo(id, name, PackSource.BUILT_IN, Optional.of( new KnownPack(MossFix.MOD_ID, id, "1.0.0"))));//i guess increment this? lol
        this.extraNamespaces = extraNamespaces.toArray(new String[0]);
        this.platform = platform;
    }

    #else

    private final String name;
    private final String description;

    public MossFixDataPack(String id, String name, String description, Set<String> extraNamespaces, PackPlatform platform) {
        super(id, true);
        this.name = name;
        this.description = description;
        this.extraNamespaces = extraNamespaces.toArray(new String[0]);
        this.platform = platform;

    }
    #endif



    public Pack toPack() {
        int packVersion = SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA);

        #if MC_NVERSION > 12004

        Pack.Metadata meta = Pack.readPackMetadata(location(), this, packVersion);

        if(meta == null) {
            throw new IllegalStateException("Could not find builtin resource pack metadata");
        }

        return Pack.readMetaAndCreate(location(), this, PackType.SERVER_DATA, new PackSelectionConfig(false, Pack.Position.TOP, false));

        #else

        Pack.Info info = Pack.readPackInfo("", this#if MC_NVERSION > 12001, packVersion #endif);

        if(info == null) {
            throw new IllegalStateException("Could not find builtin resource pack info");
        }

        return Pack.create(packId(), Component.literal(name), false, this, info, #if MC_NVERSION <= 12001PackType.SERVER_DATA,#endif Pack.Position.TOP, false, PackSource.BUILT_IN);

        #endif
    }

    private String getPath() {
        return "/" + platform.packRootPath + "/" + packId() + "/";
    }

    @Nullable
    private InputStream get(String name){
        return MossFix.class.getResourceAsStream(getPath() + name);
    }

    @Nullable
    private IoSupplier<InputStream> getResource(String path){
        InputStream resourceStream = get(path);
        if (resourceStream == null) {
            return null;
        }

        return () -> resourceStream;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... strings) {
        return getResource(String.join("/", strings));
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType packType, ResourceLocation resourceLocation) {
        return getRootResource(packType.getDirectory(), resourceLocation.getNamespace(), resourceLocation.getPath());
    }

    @Override
    public void listResources(PackType packType, final String namespace, String prefix, ResourceOutput resourceOutput) {
        try {
            URL url = MossFix.class.getResource(getPath());
            if (url == null) {
                return;
            }

            //gets the path from the dataPack directory to the pack's directory
            Path namespacePath = Paths.get(url.toURI()).resolve(packType.getDirectory()).resolve(namespace);

            //path from dataPack directory to the specific resource prefix we actually want to list (e.g. tags)
            Path resPath = namespacePath.resolve(prefix);

            if (!Files.exists(resPath)) {
                return;
            }

            try (Stream<Path> files = Files.walk(resPath)) {
                files.filter(path -> !Files.isDirectory(path)).forEach(path -> {

                    //build the location of each specific resource from a possibly resourcelocation format-nonconforming path
                    //and remove the beginning of the file location from the front of it
                    #if MC_NVERSION >= 12100

                    ResourceLocation location = ResourceLocation.fromNamespaceAndPath(
                            namespace,
                            convertPath(path).substring(
                                    convertPath(namespacePath).length() + 1
                            )
                    );

                    #else

                    ResourceLocation location = new ResourceLocation(
                            namespace,
                            convertPath(path).substring(
                                    convertPath(namespacePath).length() + 1
                            )
                    );
                    #endif


                    resourceOutput.accept(location, getResource(packType, location));
                });
            }

        } catch (Exception e) {
            MossFix.WHOLE_MOD_LOGGER.error("Failed to list tag config pack resources", e);
        }
    }


    private static String convertPath(Path path){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < path.getNameCount(); i++) {
            builder.append(path.getName(i));
            if (i < path.getNameCount() - 1) {
                builder.append("/");
            }
        }
        return builder.toString();
    }

    @Override
    public @NotNull Set<String> getNamespaces(PackType packType) {
        if(packType != PackType.SERVER_DATA)
            return ImmutableSet.of();

        // right now, these will only really contain Minecraft resources, since we're using them to configure moss spread over blocks
        return ImmutableSet.<String>builder().add("minecraft").add(extraNamespaces).build();
    }

    @Override
    public void close() { }

    #if MC_NVERSION > 12004

    @Override
    public @NotNull PackResources openPrimary(PackLocationInfo packLocationInfo) {
        return this;
    }

    @Override
    public @NotNull PackResources openFull(PackLocationInfo packLocationInfo, Pack.Metadata metadata) {
        return this;
    }

    #elif MC_NVERSION > 12001

    @Override
    public @NotNull PackResources openPrimary(String uhhh) {
        return this;
    }

    @Override
    public @NotNull PackResources openFull(String uhhh, Pack.Info info) {
        return this;
    }

    #else

    @Override
    public @NotNull PackResources open(String string) {
        return this;
    }

    #endif

    @Override
    public @Nullable <T> T getMetadataSection(MetadataSectionSerializer<T> metadataSectionSerializer) throws IOException {
        if(metadataSectionSerializer.getMetadataSectionName().equals("pack")){
            #if MC_NVERSION > 12004

            //noinspection unchecked
            return ((T) new PackMetadataSection(
                    Component.translatable("resourcepack.mossfix." + location().id() + ".description"),
                    SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA)#if MC_NVERSION >= 12002,
                    Optional.empty() #endif
            ));

            #else

            //noinspection unchecked
            return ((T) new PackMetadataSection(
                    Component.literal(description),
                    SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA)#if MC_NVERSION >= 12002,
                    Optional.empty() #endif
            ));

            #endif
        }

        return super.getMetadataSection(metadataSectionSerializer);
    }

    @SuppressWarnings("unused")
    public PackPlatform getPlatform() {
        return platform;
    }



    public enum PackPlatform {
        COMMON("dataPacks"),
        FABRIC("fabricDataPacks"),
        NEOFORGE("forgeDataPacks");

        public final String packRootPath;

        PackPlatform(String packRootPath){
            this.packRootPath = packRootPath;
        }
    }
}
