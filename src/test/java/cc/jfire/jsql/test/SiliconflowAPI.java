package cc.jfire.jsql.test;

import cc.jfire.dson.Dson;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.List;

@Slf4j
public class SiliconflowAPI
{
    private static final MediaType    JSON   = MediaType.get("application/json; charset=utf-8");
    private static       OkHttpClient client = new OkHttpClient();
    private static final String       token  = "sk-uumgwlregyemdtjuamwiavshhxmkiogbzkfrbacblocbxdsr";

    // 新增方法：发送嵌入请求
    public static float[] sendEmbeddingRequest(String input)
    {
        // 构造请求体
        RequestBody body = RequestBody.create(Dson.toJson(new EmbeddingsParam().setInput(input)), JSON);
        // 构造请求
        Request request = new Request.Builder().url("https://api.siliconflow.cn/v1/embeddings").addHeader("Authorization", "Bearer " + token).addHeader("Content-Type", "application/json").post(body).build();
        // 发送请求并获取响应
        try (Response response = client.newCall(request).execute())
        {
            if (!response.isSuccessful())
            {
                throw new IOException("Unexpected code " + response);
            }
            return ((EmbeddingsResult) Dson.fromString(EmbeddingsResult.class, response.body().string())).getData().get(0).getEmbedding();
        }
        catch (Throwable e)
        {
            log.error("出现异常", e);
            return null;
        }
    }

    @Data
    @Accessors(chain = true)
    public static class EmbeddingsParam
    {
        private       String model           = "BAAI/bge-large-zh-v1.5";
        private       String input;
        private final String encoding_format = "float";
    }

    @Data
    @Accessors(chain = true)
    public static class EmbeddingsResult
    {
        private List<EmbeddingDTO> data;
    }

    @Data
    @Accessors(chain = true)
    public static class EmbeddingDTO
    {
        private String  object;
        private float[] embedding;
        private int     index;
    }
}
