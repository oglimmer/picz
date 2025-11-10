import { reactive, ref } from "vue";
import { defineStore } from "pinia";
import { OidcClient, SigninResponse } from "oidc-client-ts";
import config from "@/Config";

interface UserCapacity {
  acceptedTos: boolean;
  email: string;
  usedCapacity: number;
  capacity: number;
  processingAlbumIds: number[];
  processingCounterByAlbum: Record<string, number>;
  numberOfImagesByAlbum: Record<number, number>;
}

const url = window.location.origin + "/login";

const client = new OidcClient({
  authority: config.idpServer,
  client_id: config.idpClientId,
  redirect_uri: url,
  post_logout_redirect_uri: url,
  scope: "openid email",
  response_mode: "query",
  filterProtocolClaims: false,
});

export const useLoginStore = defineStore("login", () => {
  const accessToken = ref("");
  const data = reactive({} as SigninResponse);
  const userCapacity = ref<UserCapacity | null>(null);

  // Track ongoing fetch request to prevent duplicates
  let ongoingFetchPromise: Promise<void> | null = null;

  const doRefresh = async () => {
    const signinResponse = await client.useRefreshToken({
      state: {
        refresh_token: data.refresh_token!,
        session_state: data.session_state,
        data: undefined,
        profile: data.profile,
        id_token: data.id_token,
        scope: data.scope,
      },
      timeoutInSeconds: 15,
    });
    await setTokens(signinResponse);
  };

  const setTokens = async (signinResponse: SigninResponse) => {
    if (signinResponse.error) {
      window.location.href = await createSigninRequest();
    } else {
      accessToken.value = signinResponse.access_token;
      Object.assign(data, signinResponse);
      setTimeout(doRefresh, signinResponse.expires_in! * 50 * 0.9);
    }
  };

  const createSigninRequest = async (): Promise<string> => {
    const req = await client.createSigninRequest({ state: {} });
    return req.url;
  };

  const processSigninResponse = async () => {
    const signinResponse = await client.processSigninResponse(
      document.location.href
    );
    await setTokens(signinResponse);
  };

  const fetchUserInfo = async (): Promise<void> => {
    if (!accessToken.value) return;

    // If there's already an ongoing request, return that promise
    if (ongoingFetchPromise) {
      return ongoingFetchPromise;
    }

    // Create a new fetch promise
    ongoingFetchPromise = (async () => {
      try {
        const response = await fetch(`${config.apiServer}/api/v1/user`, {
          headers: {
            Authorization: `Bearer ${accessToken.value}`,
          },
        });

        if (response.ok) {
          userCapacity.value = await response.json();
        }
      } catch (error) {
        console.error("Failed to fetch user capacity:", error);
      } finally {
        // Clear the promise when done
        ongoingFetchPromise = null;
      }
    })();

    return ongoingFetchPromise;
  };

  const clearUserData = () => {
    userCapacity.value = null;
    ongoingFetchPromise = null;
  };

  return {
    accessToken,
    userCapacity,
    setTokens,
    createSigninRequest,
    processSigninResponse,
    fetchUserInfo,
    clearUserData,
  };
});
