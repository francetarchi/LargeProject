import argparse
import json

import utils.constants as c
from utils.requester import Requester


def get_arguments():
    """Gets arguments from the command line.

    Returns:
        A parser with the input arguments.

    """

    parser = argparse.ArgumentParser(usage='Scraps all wine data from Vivino.')

    parser.add_argument('output_file', help='Output .json file', type=str)

    parser.add_argument('-start_page', help='Starting page identifier', type=int, default=1)

    return parser.parse_args()


if __name__ == '__main__':
    # Gathers the input arguments and its variables
    args = get_arguments()
    output_file = args.output_file
    start_page = args.start_page

    # Instantiates a wrapper over the `requests` package
    r = Requester(c.BASE_URL)

    # Defines the payload, i.e., filters to be used on the search
    payload = {
        "country_codes[]": "it",
        # "food_ids[]": 20,
        # "grape_ids[]": 3,
        # "grape_filter": "varietal",
        # "min_rating": 3.5,
        "order_by": "ratings_average",
        "order": "desc",
        # "price_range_min": 22,
        # "price_range_max": 22,
        # "region_ids[]": 383,
        # "wine_style_ids[]": 98,
        # "wine_type_ids[]": 1,
        # "wine_type_ids[]": 2,
        # "wine_type_ids[]": 3,
        # "wine_type_ids[]": 4,
        # "wine_type_ids[]": 7,
        # "wine_type_ids[]": 24,
    }

    # Performs an initial request to get the number of records (wines)
    res = r.get('explore/explore?', params=payload)
    n_matches = res.json()['explore_vintage']['records_matched']

    print(f'\n')
    print(f'Number of total matches: {n_matches}')
    print(f'Number of pages: {int(n_matches / c.RECORDS_PER_PAGE)}\n')

    exit()

    # Creates a dictionary to hold the data
    data = {}
    data['vintages'] = []
    data['wines'] = []

    # Iterates through the amount of possible pages
    for i in range(start_page, max(1, int(n_matches / c.RECORDS_PER_PAGE)) + 1):
        # Adds the page to the payload
        payload['page'] = i
        
        # Performs the request and scraps the URLs
        res = r.get('explore/explore', params=payload)
        matches = res.json()['explore_vintage']['matches']
        
        print(f'\nPage: {payload["page"]}')
        print(f"    --> num of matches: {len(matches)}")

        # printings di controllo
        # print(f"\n------------------------------------ 1st MATCH ------------------------------------")

        # print("STRUCTURE")
        # print(f'match[0].keys(): {matches[0].keys()}')
        # print(f"matches[0]['vintage'].keys(): {matches[0]['vintage'].keys()}")
        # print(f"matches[0]['vintage']['wine'].keys(): {matches[0]['vintage']['wine'].keys()}")
        # print(f"matches[0]['vintage']['wine']['style'].keys(): {matches[0]['vintage']['wine']['style'].keys()}")
        # print('\n')

        # print("VALUES")
        # print(f"matches[0]['vintage']['wine']: {matches[0]['vintage']['wine']}")
        # print(f"matches[0]['vintage']['wine']['style']: {matches[0]['vintage']['wine']['style']}")

        # print("------------------------------------ END MATCH ------------------------------------\n")
        
        # per il development, togliere il commento quando siamo sicuri della forma in cui vogliamo scaricare i dati
        # exit()

        # Iterates over every match
        avanzamento = 0
        for match in matches:
            # Gathers the vintage data
            vintage = match['vintage']
            vintage['price'] = match['price']['amount']

            # Gathers the wine data
            wine = match['vintage']['wine']
            
            # ---------------------- Working on VINTAGE ----------------------
            # Popping redundant values
            vintage.pop('seo_name', None)
            vintage.pop('image', None)  ########## forse da commentare ##########
            vintage['wine'] = vintage['wine']['id']
            vintage.pop('grapes', None)                     # tutti gli elementi a null
            vintage.pop('has_valid_ratings', None)          # tutti gli elementi a True
            vintage['statistics'].pop('wine_status', None)  # tutti gli elementi a ""

            # Move some fields to the end of the dictionary
            vintage_statistics = vintage.pop('statistics', None)
            if vintage_statistics:
                vintage['statistics'] = vintage_statistics
            vintage_top_list_rankings = vintage.pop('top_list_rankings', None)
            if vintage_top_list_rankings:
                vintage['top_list_rankings'] = vintage_top_list_rankings

            # Appends current vintage to the dictionary
            data['vintages'].append(vintage)

            # ---------------------- Working on WINE ----------------------
            # Popping redundant values
            wine.pop('seo_name', None)
            wine.pop('statistics', None)
            wine.pop('vintage_type', None)
            wine.pop('has_valid_ratings', None)
            
            if wine['region']:
                wine['region'].pop('name_en', None)
                wine['region'].pop('seo_name', None)
                wine['region'].pop('background_image', None)
                if wine['region']['country']:
                    wine['region']['country'].pop('native_name', None)
                    wine['region']['country'].pop('seo_name', None)
                    if wine['region']['country']['currency']:
                        wine['region']['country']['currency'].pop('suffix', None)
                wine['region'].pop('class', None)
                if wine['region']['country']['most_used_grapes']:
                    for grape in wine['region']['country']['most_used_grapes']:
                        grape.pop('seo_name', None)
                        grape.pop('has_detailed_info', None)
                        grape.pop('parent_grape_id', None)
                
            if wine['winery']:
                wine['winery'].pop('seo_name', None)
                wine['winery'].pop('status', None)
                wine['winery'].pop('background_image', None)
            
            if wine['style']:
                wine['style'].pop('seo_name', None)
                wine['style'].pop('regional_name', None)
                wine['style'].pop('varietal_name', None)
                wine['style'].pop('country', None)
                wine['style'].pop('region', None)
                wine['style'].pop('parent_style_id', None)
                wine['style'].pop('hidden', None)
                wine['style'].pop('vintage_mask', None)
                if wine['style']['food']:
                    for food in wine['style']['food']:
                        food.pop('seo_name', None)
                        food.pop('background_image', None)
                if wine['style']['grapes']:
                    for grape in wine['style']['grapes']:
                        grape.pop('seo_name', None)
                        grape.pop('has_detailed_info', None)
                        grape.pop('parent_grape_id', None)

            # Appends current match to the dictionary
            data['wines'].append(wine)

            # Gathers the full-taste profile from current match
            res = r.get(f'wines/{wine["id"]}/tastes')
            if res.status_code == 200:
                tastes = res.json()
                if tastes['tastes']:    # popping redundant values
                    tastes['tastes'].pop('flavor', None)
                data['wines'][-1]['taste'] = tastes['tastes']

            # Gathers the reviews from current match
            res = r.get(f'wines/{wine["id"]}/reviews')
            if res.status_code == 200:
                reviews = res.json()
                if reviews['reviews']: # popping redundant values
                    for review in reviews['reviews']:
                        review.pop('aggregated', None)
                        review.pop('user', None)
                        review.pop('vintage', None)
                        review.pop('tagged_note', None)
                data['wines'][-1]['reviews'] = reviews['reviews']

            # Print the progress percentage
            avanzamento += 4
            print(f"\r    --> avanzamento: {avanzamento}%", end="")
        print("\n")

    # Opens the output .json file in write mode
    with open(output_file, 'w') as f:
        # Dumps the data to the file
        json.dump(data, f)

    # Closes the file
    f.close()
