package dev.vanutp.multiauth.mixin;

import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.response.HasJoinedMinecraftServerResponse;
import com.mojang.authlib.yggdrasil.response.Response;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;


@Mixin(YggdrasilMinecraftSessionService.class)
public class SessionServiceMixin {
    @Unique
    private final static List<String> AUTH_SERVERS = Arrays.asList(
            "https://sessionserver.mojang.com/session/minecraft/hasJoined",
            "https://authserver.ely.by/session/hasJoined"
    );

    @Unique
    private final static List<String> ADDITIONAL_SKIN_URLS = Arrays.asList(
            "http://ely.by/storage/skins/"
    );

    @Unique
    private final static String MOJANG_AUTH_SERVER = "https://sessionserver.mojang.com/session/minecraft/hasJoined";

    @Redirect(method = "hasJoinedServer", at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/yggdrasil/YggdrasilAuthenticationService;makeRequest(Ljava/net/URL;Ljava/lang/Object;Ljava/lang/Class;)Lcom/mojang/authlib/yggdrasil/response/Response;"), remap = false)
    private Response injected(YggdrasilAuthenticationService instance, URL originalUrl, Object input, Class<HasJoinedMinecraftServerResponse> classOfT) throws Throwable {
        var makeRequest = YggdrasilAuthenticationService.class.getDeclaredMethod("makeRequest", URL.class, Object.class, Class.class);
        HasJoinedMinecraftServerResponse res = null;
        Throwable exc = null;

        for (var authServer : AUTH_SERVERS) {
            var url = new URL(originalUrl.toString().replace(MOJANG_AUTH_SERVER, authServer));
            try {
                res = (HasJoinedMinecraftServerResponse) makeRequest.invoke(instance, url, input, classOfT);
            } catch (InvocationTargetException e) {
                if (exc == null) {
                    exc = e.getCause();
                }
            }
            if (res != null) {
                return res;
            }
        }
        if (exc != null) {
            throw exc;
        } else {
            return null;
        }
    }

    @Inject(method = "isAllowedTextureDomain", at = @At("RETURN"), remap = false, cancellable = true)
    private static void isAllowedTextureDomain(String url, CallbackInfoReturnable<Boolean> cir) {
        for (var skinUrl : ADDITIONAL_SKIN_URLS) {
            if (url.startsWith(skinUrl)) {
                cir.setReturnValue(true);
            }
        }
    }
}
