package dev.vanutp.multiauth.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.response.HasJoinedMinecraftServerResponse;
import com.mojang.authlib.yggdrasil.response.Response;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;


@Mixin(YggdrasilMinecraftSessionService.class)
public class SessionServiceMixin {
    @Redirect(method = "hasJoinedServer", at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/yggdrasil/YggdrasilAuthenticationService;makeRequest(Ljava/net/URL;Ljava/lang/Object;Ljava/lang/Class;)Lcom/mojang/authlib/yggdrasil/response/Response;"), remap = false)
    private Response injected(YggdrasilAuthenticationService instance, URL url, Object input, Class<HasJoinedMinecraftServerResponse> classOfT) throws Throwable {
        var makeRequest = YggdrasilAuthenticationService.class.getDeclaredMethod("makeRequest", URL.class, Object.class, Class.class);
        HasJoinedMinecraftServerResponse mojangRes = null;
        Throwable mojangExc = null;
        try {
            mojangRes = (HasJoinedMinecraftServerResponse) makeRequest.invoke(instance, url, input, classOfT);
        } catch (InvocationTargetException e) {
            mojangExc = e.getCause();
        }
        var elybyUrl = new URL(url.toString().replace("https://sessionserver.mojang.com/session/minecraft/hasJoined", "https://authserver.ely.by/session/hasJoined"));
        HasJoinedMinecraftServerResponse elybyRes = null;
        Throwable elybyExc = null;
        try {
            elybyRes = (HasJoinedMinecraftServerResponse) makeRequest.invoke(instance, elybyUrl, input, classOfT);
        } catch (InvocationTargetException e) {
            elybyExc = e.getCause();
        }

        if (mojangRes != null) {
            return mojangRes;
        } else if (elybyRes != null) {
            return elybyRes;
        } else if (mojangExc != null) {
            throw mojangExc;
        } else if (elybyExc != null) {
            throw elybyExc;
        } else {
            return null;
        }
    }

    @Inject(method = "getTextures", at = @At("HEAD"), remap = false)
    private void getTextures(GameProfile profile, boolean requireSecure, CallbackInfoReturnable<Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>> cir) {
        System.out.println(profile.getId());
        System.out.println(profile.getName());
        for (var e : profile.getProperties().entries()) {
            System.out.println(e.getKey() + " (sign: " + e.getValue().hasSignature() + "): " + e.getValue().getValue());
        }
    }

    @Inject(method = "isAllowedTextureDomain", at = @At("RETURN"), remap = false, cancellable = true)
    private static void isAllowedTextureDomain(String url, CallbackInfoReturnable<Boolean> cir) {
        if (url.startsWith("http://ely.by/storage/skins/")) {
            cir.setReturnValue(true);
        }
    }
}
