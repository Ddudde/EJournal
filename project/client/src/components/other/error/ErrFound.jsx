import React, {useEffect, useRef} from "react";
import {Helmet} from "react-helmet-async";
import errCSS from './error.module.css';
import warn from '../../../media/warn_big.png';
import {states} from "../../../store/selector";
import {useSelector} from "react-redux";

export function ErrFound(props) {
    const cState = useSelector(states);
    const isFirstUpdate = useRef(true);
    useEffect(() => {
        console.log("I was triggered during componentDidMount ErrFound.jsx");
        return function() {
            console.log("I was triggered during componentWillUnmount ErrFound.jsx");
        }
    }, []);
    useEffect(() => {
        if (isFirstUpdate.current) {
            isFirstUpdate.current = false;
            return;
        }
        console.log('componentDidUpdate ErrNotFound.jsx');
    });
    return props.text ?
            <div className={errCSS.block}>
                <img alt="banner" src={warn}/>
                <div className={errCSS.block_text}>
                    {props.text}
                </div>
            </div>
        :
            <div className={errCSS.AppHeader}>
                <Helmet>
                    <title>Ошибка</title>
                </Helmet>
                <div className={errCSS.block}>
                    <img alt="banner" src={warn}/>
                    {!cState.auth &&
                        <div className={errCSS.block_text}>
                            К сожалению, страница не найдена... Предлагаем изучить страницы на выбор ("Школам", "Педагогам", "Родителям", "Учащимся"). <br/>Также можете авторизоваться, тогда система предложит вам подходящую страницу по изучению портала.
                        </div>
                    }
                    {(cState.auth && cState.role == 3) &&
                        <div className={errCSS.block_text}>
                            К сожалению, страница не найдена... Предлагаем изучить страницу "Школам".
                        </div>
                    }
                    {(cState.auth && cState.role == 2) &&
                        <div className={errCSS.block_text}>
                            К сожалению, страница не найдена... Предлагаем изучить страницу "Педагогам".
                        </div>
                    }
                    {(cState.auth && cState.role == 1) &&
                        <div className={errCSS.block_text}>
                            К сожалению, страница не найдена... Предлагаем изучить страницу "Родителям".
                        </div>
                    }
                    {(cState.auth && cState.role == 0) &&
                        <div className={errCSS.block_text}>
                            К сожалению, страница не найдена... Предлагаем изучить страницу "Обучающимся".
                        </div>
                    }
                </div>
            </div>
}
export default ErrFound;