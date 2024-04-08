package com.maciuszek.wordcount.testutil;

import lombok.Getter;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

@Getter
public class ResultCaptor<T> implements Answer {

    private T result;

    @Override
    public T answer(InvocationOnMock invocationOnMock) throws Throwable {
        result = (T) invocationOnMock.callRealMethod();
        return result;
    }

}
