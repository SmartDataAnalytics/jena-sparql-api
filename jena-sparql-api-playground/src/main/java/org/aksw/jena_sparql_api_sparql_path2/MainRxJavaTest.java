package org.aksw.jena_sparql_api_sparql_path2;

import rx.Observable;

public class MainRxJavaTest {
    public static void main(String[] args) {

        //Observable.from(args).subscribe()
        Observable<Object> obs = Observable.create(subscriber -> {
            for(int i = 0; i < 50; ++i) {
                if(!subscriber.isUnsubscribed()) {
                    subscriber.onNext("yay" + i);
                }
            }
            if(!subscriber.isUnsubscribed()) {
                subscriber.onCompleted();
            }
        });
        obs.subscribe(x -> System.out.println(x));

        if(true) {
            return;
        }


        //public static FrontierRDD advanceFrontier(FrontierRDD, NFA;, )
    }
}
