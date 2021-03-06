/* with char|byte|short|int|long|float|double|obj elem */
/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.collect.set.hash;

import net.openhft.collect.*;
import net.openhft.function.*;
import net.openhft.collect.set.CharSetFactory;
import javax.annotation.Nullable;

import java.util.Iterator;


/**
 * @see HashCharSets#getDefaultFactory()
 */
public interface HashCharSetFactory/*<>*/ extends CharSetFactory/*<>*/ {

    /* if obj elem */
    <E2> HashCharSetFactory<E2> withEquivalence(@Nullable Equivalence<E2> equivalence);
    /* endif */

    /* if !(float|double elem) */CharHashConfig/* elif float|double elem //HashConfig// endif */
    getConfig();

    HashCharSetFactory/*<>*/ withConfig(/* if !(float|double elem) */CharHashConfig
            /* elif float|double elem //HashConfig// endif */ config);

    /* define p1 *//* if obj elem //<E2 extends E>// endif *//* enddefine */

    /* define p2 *//* if obj elem //<E2>// endif *//* enddefine */

    /* define ep */
    /* if obj elem //<? extends E2>// elif !(obj elem) //<Character>// endif */
    /* enddefine */

    @Override
    /*p1*/ HashCharSet/*p2*/ newMutableSet();

    @Override
    /*p1*/ HashCharSet/*p2*/ newMutableSet(int expectedSize);


    /* with Mutable|Immutable mutability */

    /* with with|without expectedSize */
    /* define arg *//* if with expectedSize //, int expectedSize// endif *//* enddefine */

    /* if with expectedSize *//**
     * If the specified elements is a set// if obj elem //
     * and has the same equivalence with this factory// endif //,
     * {@code expectedSize} is ignored.
     *//* endif*/
    @Override
    /*p1*/ HashCharSet/*p2*/ newMutableSet(Iterable/*ep*/<Character>/**/ elements/*arg*/);

    @Override
    /*p1*/ HashCharSet/*p2*/ newMutableSet(Iterable/*ep*/<Character>/**/ elems1,
            Iterable/*ep*/<Character>/**/ elems2/*arg*/);

    @Override
    /*p1*/ HashCharSet/*p2*/ newMutableSet(Iterable/*ep*/<Character>/**/ elems1,
            Iterable/*ep*/<Character>/**/ elems2, Iterable/*ep*/<Character>/**/ elems3/*arg*/);

    @Override
    /*p1*/ HashCharSet/*p2*/ newMutableSet(Iterable/*ep*/<Character>/**/ elems1,
            Iterable/*ep*/<Character>/**/ elems2, Iterable/*ep*/<Character>/**/ elems3,
            Iterable/*ep*/<Character>/**/ elems4/*arg*/);

    @Override
    /*p1*/ HashCharSet/*p2*/ newMutableSet(Iterable/*ep*/<Character>/**/ elems1,
            Iterable/*ep*/<Character>/**/ elems2, Iterable/*ep*/<Character>/**/ elems3,
            Iterable/*ep*/<Character>/**/ elems4, Iterable/*ep*/<Character>/**/ elems5/*arg*/);

    /* endwith */

    @Override
    /*p1*/ HashCharSet/*p2*/ newMutableSet(Iterator/*ep*/<Character>/**/ elements);

    @Override
    /*p1*/ HashCharSet/*p2*/ newMutableSet(Iterator/*ep*/<Character>/**/ elements,
            int expectedSize);

    @Override
    /*p1*/ HashCharSet/*p2*/ newMutableSet(Consumer</*f*/CharConsumer/*p2*/> elementsSupplier);

    @Override
    /*p1*/ HashCharSet/*p2*/ newMutableSet(Consumer</*f*/CharConsumer/*p2*/> elementsSupplier,
            int expectedSize);

    /* define pe *//* if !(obj elem) //char// elif obj elem //E2// endif *//* enddefine */

    @Override
    /*p1*/ HashCharSet/*p2*/ newMutableSet(/*pe*/char/**/[] elements);

    @Override
    /*p1*/ HashCharSet/*p2*/ newMutableSet(/*pe*/char/**/[] elements, int expectedSize);

    /* if !(obj elem) */
    @Override
    /*p1*/ HashCharSet/*p2*/ newMutableSet(Character[] elements);

    @Override
    /*p1*/ HashCharSet/*p2*/ newMutableSet(Character[] elements, int expectedSize);
    /* endif */

    @Override
    /*p1*/ HashCharSet/*p2*/ newMutableSetOf(/*pe*/char/**/ e1);

    @Override
    /*p1*/ HashCharSet/*p2*/ newMutableSetOf(/*pe*/char/**/ e1, /*pe*/char/**/ e2);

    @Override
    /*p1*/ HashCharSet/*p2*/ newMutableSetOf(/*pe*/char/**/ e1, /*pe*/char/**/ e2,
            /*pe*/char/**/ e3);

    @Override
    /*p1*/ HashCharSet/*p2*/ newMutableSetOf(/*pe*/char/**/ e1, /*pe*/char/**/ e2,
            /*pe*/char/**/ e3, /*pe*/char/**/ e4);

    @Override
    /*p1*/ HashCharSet/*p2*/ newMutableSetOf(/*pe*/char/**/ e1, /*pe*/char/**/ e2,
            /*pe*/char/**/ e3, /*pe*/char/**/ e4, /*pe*/char/**/ e5,
            /*pe*/char/**/... restElements);
    /* endwith */
}
