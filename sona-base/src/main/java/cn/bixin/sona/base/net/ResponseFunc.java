package cn.bixin.sona.base.net;

import io.reactivex.functions.Function;

public class ResponseFunc<T> implements Function<ResponseResult<T>, T> {

    @Override
    public T apply(ResponseResult<T> response) throws Exception {
        if (ResponseResult.isSuccess(response.getCode())) {
            if (response.getData() == null) {
                throw new ApiException(response.getCode(), response.getMsg());
            }
            return response.getData();
        }
        throw new ApiException(response.getCode(), response.getMsg(), response.getData(), response.getExt());
    }

}