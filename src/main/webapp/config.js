import React from 'react';

export const stateKey = 'ticks';

export const plugins = {};

export const columns = [
    {
        name: 'Exchange',
        dataIndex: 'exchange',
        sortable: false,
        className: 'additional-class',
        createKeyFrom: true
    },
    {
        name: 'Timestamp',
        dataIndex: 'timestamp',
        sortable: false,
        className: 'additional-class',
        renderer: ({ value }) => {
        	var date = new Date(value);

            return <span>{ date.toTimeString() }</span>;
        }
    },
    {
        name: 'Bid',
        dataIndex: 'bid',
        sortable: false,
        className: 'additional-class',
        renderer: ({ value }) => {
        	if(value === 0) {
           		return <span><i>No Data</i></span>;
        	} else { 
            	return <span>{ Number(value).toFixed(2) }</span>; 
            }
        }
    },
    {
        name: 'Ask',
        dataIndex: 'ask',
        sortable: false,
        className: 'additional-class',
        renderer: ({ value }) => {
        	if(value === 0) {
           		return <span><i>No Data</i></span>;
        	} else { 
            	return <span>{ Number(value).toFixed(2) }</span>; 
            }
        }
    },
    {
    	name: 'Status',
    	dataIndex: 'status',
    	sortable: false,
    	className: 'additional-class'
    }
];
